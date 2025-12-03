package global.aop.proxy;

import global.aop.log.Log;
import global.aop.log.LogConfig;
import global.aop.transaction.Transactional;
import java.lang.reflect.Method;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.slf4j.Logger;

public class CglibAopProxy implements MethodInterceptor {

    private final Object target;

    public CglibAopProxy(Object target) {
        this.target = target;
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        // @Log 어노테이션이 있으면 메소드 호출 전 로깅
        long time = 0;
        if (method.isAnnotationPresent(Log.class)) {
            time = System.currentTimeMillis();
            Logger log = LogConfig.getLogger(target);
            log.info("[{}] {} 메서드 호출", target.getClass().getSimpleName(), method.getName());
        }

        Object result;

        // @Transactional 어노테이션이 있으면 트랜잭션 시작
        if (method.isAnnotationPresent(Transactional.class)) {
            // 트랜잭션 관련 로직을 여기다 작성하자
        }

        result = method.invoke(target, objects);

        if (method.isAnnotationPresent(Log.class)) {
            time = System.currentTimeMillis() - time;
            Logger log = LogConfig.getLogger(target);
            log.info("[{}] {} 메서드 호출 종료, 소요시간: {} ms",
                    target.getClass().getSimpleName(),
                    method.getName(),
                    time);
        }

        return result;
    }

    public static <T> T createProxy(T target, Class<T> clazz) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setCallback(new CglibAopProxy(target));
        return (T) enhancer.create();
    }
}
