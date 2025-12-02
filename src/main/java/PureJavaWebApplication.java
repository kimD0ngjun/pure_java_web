import global.ioc.IocContainerConfig;
import global.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Log
public class PureJavaWebApplication {

    private static Logger log = LoggerFactory.getLogger(PureJavaWebApplication.class);

    public static void main(String[] args) {
        log.info("""
                
                =====================================
                PURE JAVA WEB APPLICATION RUNNING...!
                =====================================
                """);

        // 1. IoC 컨테이너 초기화
        IocContainerConfig ioc = new IocContainerConfig();
        ioc.run();

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

        // 3. JVM 종료 시 IoC 컨테이너 종료 등록
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("IoC container 종료 중...");
            ioc.shutdown();
            log.info("Shutdown complete. GOOD BYE!");
        }));

        // 현재는 스레드 강제 점유로 동작하는 것처럼 보이게 하기
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            log.error("Main thread interrupted", e);
            Thread.currentThread().interrupt();
        }
    }
}
