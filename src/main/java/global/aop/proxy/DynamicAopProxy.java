package global.aop.proxy;

import global.aop.log.Log;
import global.aop.log.LogConfig;
import global.aop.transaction.Transactional;
import java.lang.reflect.Proxy;
import org.slf4j.Logger;

public class DynamicAopProxy {

    public static <T> T createProxy(T target, Class<T> type) {
        // 다이나믹 프록시 생성 -> AOP 적용
        return (T) Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[]{type},
                (proxy, method, args) -> {
                    // @Log 어노테이션이 있으면 메소드 호출 전 로깅
                    long time = 0;
                    if (method.isAnnotationPresent(Log.class)) {
                        time = System.currentTimeMillis();
                        Logger log = LogConfig.getLogger(target);
                        log.info("[{}] {} 메서드 호출", target.getClass().getSimpleName(), method.getName());
                    }

                    Object result = null;

                    // @Transactional 어노테이션이 있으면 트랜잭션 시작
                    if (method.isAnnotationPresent(Transactional.class)) {
                        // 트랜잭션 관련 로직을 여기다 작성하자
                    }

                    result = method.invoke(target, args);

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
        );
    }

}
