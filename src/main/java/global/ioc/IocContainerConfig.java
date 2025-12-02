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
    private Set<Class<? extends AbstractBean>> beanClasses =
            new Reflections(new ConfigurationBuilder().forPackages("global", "domain"))
                    .getSubTypesOf(AbstractBean.class); // 패키지 스캔 후, 추상 빈 상속구현체 전부 갖고오기
    private Map<Class<?>, AbstractBean> singletonBeans = new HashMap<>(); // 싱글톤 빈 캐시

    // 의존성 주입 위상 정렬을 위한 그래프 필드 + 진입차수
    private Map<Class<?>, Set<Class<?>>> dependencyGraph = new HashMap<>();
    private Map<Class<?>, Integer> indegree = new HashMap<>();

    /**
     * IoC 컨테이너 동작 메소드
     */
    public void run() {
        // 0. IoC 로깅용 Logger 주입, 흐름상 IoC 먼저 띄우고 로그 설정 init
        LogConfig.initializeLogger(this);
        log.info("[{}] : IoC 컨테이너 동작 시작", this.getClass().getSimpleName());

        // 1. 빈 생성 & 빈 간 의존성 관리(IoC 띄운 다음에 수행하므로 리플랙션 기반)
        initAutowiredConstructorBeans(); // 위상정렬 기반 빈 생성 및 의존성 주입

        // 2. 빈 초기화(AbstractBean의 init 메소드 호출)
        for (Map.Entry<Class<?>, AbstractBean> entry: singletonBeans.entrySet()) {
            entry.getValue().init();
        }

        // 3. 서버 종료시, 빈 destroy
        for (Map.Entry<Class<?>, AbstractBean> entry: singletonBeans.entrySet()) {
            entry.getValue().destroy();
        }

        // 4. 서버 종료시, 빈 관련 필드들 전부 정리
        beanClasses = null;
        singletonBeans = null;
        dependencyGraph = null;
        indegree = null;
    }

    /**
     * <p>1. 이미 초기화 완료한 기본 생성자 기반 빈이 생성자 주입의 재료가 된다</p>
     * <p>2. 우선 순위는 기본 생성자 빈의 의존성 주입 빈 -> 의존성 주입 빈의 또다른 의존성 주입 빈으로 이뤄져야만 한다</p>
     * <p>3. 이를 위해 IoC 컨테이너에서는 어떤 알고리즘을 쓸까? : 위상정렬 생각했는데 진짜 IoC도 위상정렬 쓰네 ㅇㅂㅇ</p>
     * <p>좀더 정확히는 유향 비순환 그래프를 기반으로 의존성 구조를 형성(https://umbum.dev/876/?utm_source=chatgpt.com)</p>
     * <p>4. 의존성 주입이 여러 개라면? -> 일단 가장 파라미터 많은 생성자로만 하자...</p>
     * <p>5. 객체 회수할 때는 어떻게 할까? -> 싱글톤 캐시를 비워버리고 GC는 자동으로 객체 할당 해제</p>
     */
    private void initAutowiredConstructorBeans() {
        // 진입차수 0인 애들(기본 생성자 빈)부터 큐 산입
        Queue<Class<? extends AbstractBean>> queue = new LinkedList<>();
        for (Map.Entry<Class<?>, Integer> entry : indegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add((Class<? extends AbstractBean>) entry.getKey());
            }
        }

        // 위상정렬 시작
        while(!queue.isEmpty()) {
            Class<? extends AbstractBean> beanClass = queue.poll();

            try {
                // 생성자 선택 (파라미터가 가장 많은 생성자)
                Constructor<?> constructor = Arrays.stream(beanClass.getDeclaredConstructors())
                        .max(Comparator.comparingInt(Constructor::getParameterCount))
                        .orElseThrow(() -> new RuntimeException(beanClass.getSimpleName() + " 빈 생성자 확인 안됨"));

                // 생성자 파라미터를 싱글톤 캐시에서 가져와 준비
                Class<?>[] paramTypes = constructor.getParameterTypes();
                Object[] params = Arrays.stream(paramTypes)
                        .map(singletonBeans::get) // 이미 순차적으로 싱글톤 캐싱 처리됐을 거야
                        .toArray();

                constructor.setAccessible(true);
                AbstractBean bean = (AbstractBean) constructor.newInstance(params);

                // 싱글톤 캐싱
                singletonBeans.put(beanClass, bean);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                log.error("빈 생성 실패: {}", e.getMessage());
                throw new RuntimeException(e);
            }

            // 의존성 그래프에서 연결된 후속 빈 진입차수 감소
            for (Class<?> adj : dependencyGraph.get(beanClass)) {
                indegree.put(adj, indegree.get(adj) - 1);
                if (indegree.get(adj) == 0) { // 진입차수 0이면 큐 산입
                    queue.add((Class<? extends AbstractBean>) adj);
                }
            }
        }
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
            if (beanClass.getDeclaredConstructors().length == 1
                    && beanClass.getDeclaredConstructors()[0].getParameterCount() == 0) {
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
}
