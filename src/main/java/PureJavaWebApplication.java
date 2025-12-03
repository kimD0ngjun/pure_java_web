import global.context.ApplicationContext;
import global.ioc.IocContainer;
import global.ioc.IocContainerConfig;

public class PureJavaWebApplication {
    /**
     * 스프링 컨텍스트는 IoC 컨테이너의 생명주기, 설정정보 리딩 등을 담당한다. 그 후에 톰캣 내장 서버가 돌아간다
     * @param args
     */
    public static void main(String[] args) {

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

        ApplicationContext context = new ApplicationContext(
                new IocContainer(new IocContainerConfig())
        );

        context.run();

        // 현재는 스레드 강제 점유로 동작하는 것처럼 보이게 하기
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}
