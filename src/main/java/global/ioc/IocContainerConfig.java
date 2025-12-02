package global.ioc;

import global.log.Log;
import global.log.LogConfig;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;

@Log
public class IocContainerConfig {

    private Logger log;
    private final Set<Class<? extends AbstractBean>> beanClasses =
            new Reflections(new ConfigurationBuilder().forPackages("global", "domain"))
                    .getSubTypesOf(AbstractBean.class); // 패키지 스캔 후, 추상 빈 상속구현체 전부 갖고오기
    private final Map<Class<?>, AbstractBean> singletonBeans = new HashMap<>(); // 싱글톤 빈 캐시

    // 의존성 주입 위상 정렬을 위한 그래프 필드 + 진입차수
    private final Map<Class<?>, Set<Class<?>>> dependencyGraph = new HashMap<>();
    private final Map<Class<?>, Integer> indegree = new HashMap<>();

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
     * 자동 생성자 주입 빈 생성
     */
    private void initAutowiredConstructorBeans() {
        // 1. 이미 초기화 완료한 기본 생성자 기반 빈이 생성자 주입의 재료가 된다
        // 2. 우선 순위는 기본 생성자 빈의 의존성 주입 빈 -> 의존성 주입 빈의 또다른 의존성 주입 빈으로 이뤄져야만 한다
        // 3. 이를 위해 IoC 컨테이너에서는 어떤 알고리즘을 쓸까? : 위상정렬 생각했는데 진짜 IoC도 위상정렬 쓰네 ㅇㅂㅇ
        // 좀더 정확히는 유향 비순환 그래프를 기반으로 의존성 구조를 형성(https://umbum.dev/876/?utm_source=chatgpt.com)
        // 4. 의존성 주입이 여러 개라면?
        // 5. 객체 회수할 때는 어떻게 할까? -> 싱글톤 캐시를 비워버리고 GC는 자동으로 객체 할당 해제
    }

    /**
     * 의존성 그래프 생성
     */
    private void setDependencyGraph() {
        // 먼저 모든 빈을 dependencyGraph에 등록
        for (Class<? extends AbstractBean> beanClass : beanClasses) {
            dependencyGraph.putIfAbsent(beanClass, new HashSet<>());
        }

        for (Class<? extends AbstractBean> beanClass : beanClasses) {
            // 기본 생성자는 제외
            if (singletonBeans.containsKey(beanClass)) {
                indegree.put(beanClass, 0); // 진입차수 0
                continue;
            }

            // 생성자 주입 팬인 사이즈 검증
            // 원래대로라면 Autowired 어노테이션 체킹이나 필드, 세터 등도 파악해야되지만... 그러면 구현이 너무 빡세...
            // 그래서 일단은 생성자 주입만 검증하자... 걔중에 파라미터 개수가 가장 많은걸 중심으로(사실상 생성자 1개라고 띵킹...)
            Constructor<?> constructor = Arrays.stream(beanClass.getDeclaredConstructors())
                    .max(Comparator.comparingInt(Constructor::getParameterCount))
                    .orElseThrow(() -> new RuntimeException(beanClass.getSimpleName() + " 빈에서 생성자 확인이 안됨"));

            int paramCount = 0;
            for (Class<?> parameter : constructor.getParameterTypes()) {
                if (!AbstractBean.class.isAssignableFrom(parameter)) continue; // 추상 빈 타입만
                dependencyGraph.get(parameter).add(beanClass); // 의존성 타입 -> 빈 유향간선 추가
                paramCount++;
            }
            indegree.put(beanClass, paramCount); // 진입차수 카운팅
        }
    }

    /**
     * 싱글톤 캐시에서 빈 조회하기
     */
    private <T extends AbstractBean> T getBean(Class<T> beanClass) {
        return beanClass.cast(singletonBeans.get(beanClass));
    }
}
