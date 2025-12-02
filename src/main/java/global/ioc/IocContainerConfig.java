package global.ioc;

import global.log.Log;
import global.log.LogConfig;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;

@Log
public class IocContainerConfig {

    private Logger log;
    private Map<Class<?>, AbstractBean> singletonBeans = new HashMap<>();

    /**
     * IoC 컨테이너 동작 메소드
     */
    public void run() {
        // 0. IoC 로깅용 Logger 주입, 흐름상 IoC 먼저 띄우고 로그 설정 init
        LogConfig.initializeLogger(this);
        log.info("{} : IoC 컨테이너 동작 시작", this.getClass().getSimpleName());

        // 1. 빈 생성 & 빈 간 의존성 관리
        // 런타임에 빈 생성과 의존성을 주입(얘는 리플렉션)하면서 자동 처리하기


        // 2. 빈에 로거 주입

        // 3. 빈 초기화(AbstractBean의 init 메소드 호출

        // 4. 빈 캐싱(싱글톤 관리)

        // 5. 서버 종료시, 빈 destroy
    }
}
