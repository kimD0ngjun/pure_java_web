package global.context;

import global.ioc.IocContainer;
import global.ioc.IocContainerConfig;
import global.log.Log;
import global.log.LogConfig;
import global.reader.YmlConfigReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Log
public class ApplicationContext {
    private Logger log = LoggerFactory.getLogger(ApplicationContext.class);
    private final IocContainer ioc;
    private final YmlConfigReader reader;

    public ApplicationContext(IocContainer ioc, YmlConfigReader reader) {
        this.ioc = ioc;
        this.reader = reader;
    }

    /**
     * 편의성 static run 메소드
     */
    public static ApplicationContext run() {
        ApplicationContext context = new ApplicationContext(
                new IocContainer(new IocContainerConfig()),
                new YmlConfigReader("application.yml")
        );
        context.runContext(); // 컨텍스트 실행
        return context;
    }

    public void runContext() {
        log.info("""
                \n
                =====================================
                PURE JAVA WEB APPLICATION RUNNING...!
                =====================================
                """);

        // ioc 부팅 & 셧다운 훅 등록
        ioc.run();

        log.info("서버 포트번호: {}", reader.getConfig().getServer().getPort());
        log.info("서버 호스트: {}", reader.getConfig().getServer().getHost());
        log.info("로깅 레벨: {}", reader.getConfig().getLogging().getLevel());
        log.info("데이터베이스 url: {}", reader.getConfig().getDatabase().getUrl());
        log.info("데이터베이스 username: {}", reader.getConfig().getDatabase().getUsername());
        log.info("데이터베이스 password: {}", reader.getConfig().getDatabase().getPassword());

        // ioc 부팅 후에 설정정보 리딩
        // 설정정보 리딩 후에 톰캣 서버 부팅
//        // 2. 웹 서버 쓰레드 시작(아마 이런 식으로 구현하게 될듯?)
//        // 실제로 컨테이너 초기화 & 빈 등록 끝나면 톰캣 같은 웹 서버 시작
//        // 내장 톰캣 인스턴스 생성 후 커넥터 시작해서 HTTP 포트 바인딩 처리
//        // 톰캣 내부에서 요청 처리하는 스레드 풀 가동
//        // (메인 스레드 블록 안 시키고 리스닝 스레드와 요청처리 스레드가 비동기로 동작함)
//        Thread webServerThread = new Thread(() -> {
//            SimpleHttpServer server = new TomcatHttpServer(8080); // 포트 바인딩
//            server.start(); // 요청 처리 루프
//        }, "WebServerThread");
//
//        webServerThread.start();

        registerShutdownHook();
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Application 종료 중...");
            ioc.shutdown();
            log.info("Shutdown complete. GOOD BYE!");
        }));
    }
}
