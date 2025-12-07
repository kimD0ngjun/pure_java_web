package global.aop.proxy;

import global.aop.log.Log;
import global.aop.log.LogConfig;
import global.aop.transaction.Transactional;
import java.lang.reflect.Method;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType.Builder.MethodDefinition.ReceiverTypeDefinition;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy.Default;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.matcher.ElementMatchers;
import org.slf4j.Logger;

public class ByteBuddyAopProxy {

    /**
     * 어노테이션 기반 메소드 인터셉트 프록시
     * @param target
     * @param beanClass
     * @return
     * @param <T>
     */
    public static <T> T createProxy(T target, Class<? extends T> beanClass) {
        try {
            ReceiverTypeDefinition<Object> intercept = new ByteBuddy()
                    .subclass(Object.class)
                    .implement(beanClass.getInterfaces()) // Bean 인터페이스
                    .defineField("target", beanClass, Visibility.PRIVATE)
                    .method(ElementMatchers.any()) // AOP 적용할 메소드
                    .intercept(MethodDelegation.to(new Interceptor(target)));

            Class<?> proxyClass = intercept.make()
                    .load(beanClass.getClassLoader(), Default.INJECTION)
                    .getLoaded();

            return (T) proxyClass.getConstructor().newInstance();

        } catch (Exception e) {
            throw new RuntimeException("프록시 생성 실패: " + beanClass.getName(), e);
        }
    }

    public static class Interceptor {
        private final Object target;
        private final Logger log;

        public Interceptor(Object target) {
            this.target = target;
            this.log = LogConfig.getLogger(target);
        }

        @RuntimeType
        public Object intercept(@Origin Method method, @AllArguments Object[] args) throws Throwable {
            boolean isLog = method.isAnnotationPresent(Log.class);
            boolean isTx = method.isAnnotationPresent(Transactional.class);

            long time = 0;
            if (isLog) {
                time = System.currentTimeMillis();
                log.info("[{}] {} 호출 시작", target.getClass().getSimpleName(), method.getName());
            }

            Object result;
            if (isTx) {
                // 트랜잭션 시작
                // 예: TransactionManager.begin();
                try {
                    result = method.invoke(target, args);
                    // 트랜잭션 커밋
                } catch (Throwable t) {
                    // 트랜잭션 롤백
                    throw t;
                }
            } else {
                result = method.invoke(target, args);
            }

            if (isLog) {
                log.info("[{}] {} 호출 종료, 소요시간: {} ms",
                        target.getClass().getSimpleName(),
                        method.getName(),
                        System.currentTimeMillis() - time);
            }

            return result;
        }
    }
}
