package global.ioc;

import global.log.Log;
import global.log.LogConfig;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;

@Log
public class IocContainerConfig {

    private Logger log;
    private final Map<Class<?>, AbstractBean> singletonBeans = new HashMap<>();

    /**
     * IoC 컨테이너 동작 메소드
     */
    public void run() {
        // 0. IoC 로깅용 Logger 주입, 흐름상 IoC 먼저 띄우고 로그 설정 init
        LogConfig.initializeLogger(this);
        log.info("[{}] : IoC 컨테이너 동작 시작", this.getClass().getSimpleName());

        // 1. 빈 생성 & 빈 간 의존성 관리(IoC 띄운 다음에 수행하므로 리플랙션 기반)
        initBasicConstructorBeans(); // 기본 생성자 보유 AbstractBean 구현체들 먼저 newInstance() 호출

        // 2. 빈에 로거 주입

        // 3. 빈 초기화(AbstractBean의 init 메소드 호출

        // 4. 빈 캐싱(싱글톤 관리)

        // 5. 서버 종료시, 빈 destroy
    }

    /**
     * 기본 생성자만 있는 빈 생성
     */
    private void initBasicConstructorBeans() {
        // 패키지 스캔
        Reflections reflections = new Reflections(
                new ConfigurationBuilder().forPackages("global", "domain")
        );

        // AbstractBean 상속 구현체 전부 갖고오기
        Set<Class<? extends AbstractBean>> beanClasses =
                reflections.getSubTypesOf(AbstractBean.class);

        // 기본 생성자만 있는 빈들 생성하기
        for (Class<? extends AbstractBean> beanClass: beanClasses) {
            try {
                // 생성자가 1개이며, 생성자 파라미터가 없으면 기본 생성자
                if (beanClass.getDeclaredConstructors().length == 1
                        && beanClass.getDeclaredConstructors()[0].getParameterCount() == 0) {
                    Constructor<? extends AbstractBean> constructor = beanClass.getConstructor();
                    constructor.setAccessible(true); // private 회피
                    AbstractBean bean = constructor.newInstance();

                    bean.init(); // 빈 초기화
                    singletonBeans.put(beanClass, bean); // 싱글톤 캐싱
                }
            } catch (NoSuchMethodException
                     | IllegalAccessException
                     | InstantiationException
                     | InvocationTargetException e) {
                log.error("{} 생성자 생성 리플렉션 예외 발생: {}", e.getClass().getSimpleName(), e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 싱글톤 캐시에서 빈 조회하기
     */
    private <T extends AbstractBean> T getBean(Class<T> beanClass) {
        return beanClass.cast(singletonBeans.get(beanClass));
    }
}
