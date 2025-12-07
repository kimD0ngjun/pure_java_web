package global.ioc;

import global.aop.log.Log;
import global.aop.proxy.ByteBuddyAopProxy;
import global.aop.transaction.Transactional;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IocContainerConfig {

    private Logger log = LoggerFactory.getLogger(IocContainerConfig.class);
    private Set<Class<? extends Bean>> beanClasses;
    private Map<Class<?>, Bean> singletonBeans = new HashMap<>(); // 싱글톤 빈 캐시

    // 의존성 주입 위상 정렬을 위한 그래프 필드 + 진입차수
    private Map<Class<?>, Set<Class<?>>> dependencyGraph = new HashMap<>();
    private Map<Class<?>, Integer> indegree = new HashMap<>();

    /**
     * 빈 스캔 시작
     */
    public void scanBeans() {
        beanClasses = new Reflections(new ConfigurationBuilder().forPackages("global", "domain"))
                .getSubTypesOf(Bean.class); // 패키지 스캔 후, 추상 빈 상속구현체 전부 갖고오기
    }

    /**
     * <p>1. 이미 초기화 완료한 기본 생성자 기반 빈이 생성자 주입의 재료가 된다</p>
     * <p>2. 우선 순위는 기본 생성자 빈의 의존성 주입 빈 -> 의존성 주입 빈의 또다른 의존성 주입 빈으로 이뤄져야만 한다</p>
     * <p>3. 이를 위해 IoC 컨테이너에서는 어떤 알고리즘을 쓸까? : 위상정렬 생각했는데 진짜 IoC도 위상정렬 쓰네 ㅇㅂㅇ</p>
     * <p>좀더 정확히는 유향 비순환 그래프를 기반으로 의존성 구조를 형성(https://umbum.dev/876/?utm_source=chatgpt.com)</p>
     * <p>4. 의존성 주입이 여러 개라면? -> 일단 가장 파라미터 많은 생성자로만 하자...</p>
     * <p>5. 객체 회수할 때는 어떻게 할까? -> 싱글톤 캐시를 비워버리고 GC는 자동으로 객체 할당 해제</p>
     */
    public void constructAutowiredBeans() {
        // 진입차수 0인 애들(기본 생성자 빈)부터 큐 산입
        Queue<Class<? extends Bean>> queue = new LinkedList<>();
        for (Map.Entry<Class<?>, Integer> entry : indegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add((Class<? extends Bean>) entry.getKey());
            }
        }

        // 위상정렬 시작
        while(!queue.isEmpty()) {
            Class<? extends Bean> beanClass = queue.poll();

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
                Bean bean = (Bean) constructor.newInstance(params);

                // 프록시 감싸기
                bean = applyProxy(beanClass, bean);

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
                    queue.add((Class<? extends Bean>) adj);
                }
            }
        }

        // 위상정렬 후에도 진입차수 0 갱신이 안됐다면 순환참조
        Set<Class<? extends Bean>> remainingBeans = new HashSet<>();
        indegree.entrySet().stream()
                .filter(e -> e.getValue() != 0)
                .forEach(e ->
                        remainingBeans.add((Class<? extends Bean>) e.getKey()));

        // 순환참조 DFS 검사
        if (!remainingBeans.isEmpty()) detectAndLogCycles(remainingBeans);
    }

    /**
     * 의존성 그래프 생성
     */
    public void setDependencyGraph() {
        // 먼저 모든 빈을 dependencyGraph에 등록
        for (Class<? extends Bean> beanClass : beanClasses) {
            dependencyGraph.putIfAbsent(beanClass, new HashSet<>());
        }

        for (Class<? extends Bean> beanClass : beanClasses) {
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
                if (!Bean.class.isAssignableFrom(parameter)) continue; // 추상 빈 타입만
                dependencyGraph.get(parameter).add(beanClass); // 의존성 타입 -> 빈 유향간선 추가
                paramCount++;
            }
            indegree.put(beanClass, paramCount); // 진입차수 카운팅
        }
    }


    /**
     * 순환참조 DFS 검사
     */
    private void detectAndLogCycles(Set<Class<? extends Bean>> noZeroBeans) {
        Set<Class<?>> visited = new HashSet<>();
        Set<Class<?>> stack = new HashSet<>();
        List<List<Class<?>>> cycles = new ArrayList<>();

        for (Class<?> beanClass : noZeroBeans) {
            if (!visited.contains(beanClass)) {
                detectCyclesDFS(beanClass, visited, stack, new ArrayList<>(), cycles);
            }
        }

        if (!cycles.isEmpty()) {
            for (List<Class<?>> cycle : cycles) {
                String path = cycle.stream()
                        .map(Class::getSimpleName)
                        .reduce((a, b) -> a + " -> " + b)
                        .orElse("");
                log.error("순환참조 발생: {}", path);
            }
            throw new RuntimeException("빈들 간 순환참조는 불가능");
        }
    }

    private void detectCyclesDFS(
            Class<?> current,
            Set<Class<?>> visited,
            Set<Class<?>> stack,
            List<Class<?>> path,
            List<List<Class<?>>> cycles
    ) {
        visited.add(current);
        stack.add(current);
        path.add(current);

        for (Class<?> neighbor : dependencyGraph.getOrDefault(current, Collections.emptySet())) {
            if (!visited.contains(neighbor)) {
                detectCyclesDFS(neighbor, visited, stack, new ArrayList<>(path), cycles);
            } else if (stack.contains(neighbor)) {
                int idx = path.indexOf(neighbor);
                if (idx >= 0) {
                    cycles.add(new ArrayList<>(path.subList(idx, path.size())));
                }
            }
        }

        stack.remove(current);
    }

    /**
     * 빈에 프록시 감싸기 처리(인터페이스 기반 검증)
     * @param beanClass
     * @param originalBean
     * @return
     */
    private Bean applyProxy(Class<? extends Bean> beanClass, Bean originalBean) {
        // 1. 인터페이스가 없으면 프록시 불가: 원본 반환
        Class<?>[] interfaces = beanClass.getInterfaces();
        if (interfaces.length == 0) {
            return originalBean;
        }

        // 2. 메소드에 프록시 어노테이션 @Log, @Transactional 있는지 확인
        boolean needsProxy = Arrays.stream(beanClass.getMethods())
                .anyMatch(m -> m.isAnnotationPresent(Log.class) || m.isAnnotationPresent(Transactional.class));
        if (!needsProxy) return originalBean;

        // 3. 프록시 생성
        Bean proxy = ByteBuddyAopProxy.createProxy(originalBean, originalBean.getClass());
        return proxy;
    }

    /**
     * 빈 초기화
     */
    public void initBeans() {
        for (Map.Entry<Class<?>, Bean> entry: singletonBeans.entrySet()) {
            entry.getValue().init();
        }
    }

    /**
     * 빈 조회
     */
    public <T> T getBean(Class<T> type) {
        return (T) singletonBeans.get(type);
    }

    /**
     * IoC 컨테이너 빈 정리
     */
    public void destroyBeans() {
        // 빈 destroy
        for (Map.Entry<Class<?>, Bean> entry: singletonBeans.entrySet()) {
            entry.getValue().destroy();
        }

        // 빈 관련 필드들 전부 정리
        beanClasses = null;
        singletonBeans = null;
        dependencyGraph = null;
        indegree = null;
    }
}
