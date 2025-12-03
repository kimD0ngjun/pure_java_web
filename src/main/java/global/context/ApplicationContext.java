package global.context;

import global.ioc.IocContainer;
import global.log.Log;
import global.log.LogConfig;
import org.slf4j.Logger;

@Log
public class ApplicationContext {
    private Logger log;
    private final IocContainer ioc;

    public ApplicationContext(IocContainer ioc) {
        this.ioc = ioc;
    }

    public void run() {
        LogConfig.initializeLogger(this);
        log.info("""
                
                =====================================
                PURE JAVA WEB APPLICATION RUNNING...!
                =====================================
                """);

        // ioc 부팅 & 셧다운 훅 등록
        ioc.run();
        registerShutdownHook();
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("IoC container 종료 중...");
            ioc.shutdown();
            log.info("Shutdown complete. GOOD BYE!");
        }));
    }
}
