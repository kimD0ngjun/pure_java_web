package global.ioc;

import global.log.Log;
import global.log.LogConfig;
import org.slf4j.Logger;

@Log
public class IocContainer {

    private Logger log;
    private final IocContainerConfig config;

    public IocContainer(IocContainerConfig config) {
        this.config = config;
    }

    /**
     * IoC 컨테이너 동작 메소드
     */
    public void run() {
        // IoC 로깅용 Logger 주입, 흐름상 IoC 먼저 띄우고 로그 설정 init
        LogConfig.initializeLogger(this);
        log.info("[{}] : IoC 컨테이너 동작 시작", this.getClass().getSimpleName());

        // 빈 스캔
        config.scanBeans();

        // 빈 생성 & 빈 간 의존성 관리 + 초기화(IoC 띄운 다음에 수행하므로 리플랙션 기반)
        config.setDependencyGraph();
        config.constructAutowiredBeans(); // 위상정렬 기반 빈 생성 및 의존성 주입
        config.initBeans();
    }

    public void shutdown() {
        config.destroyBeans();
    }

    public <T> T getBean(Class<T> type) {
        return config.getBean(type);
    }
}
