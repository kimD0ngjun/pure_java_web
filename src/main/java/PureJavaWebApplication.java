import global.context.ApplicationContext;

public class PureJavaWebApplication {
    /**
     * 스프링 컨텍스트는 IoC 컨테이너보다 더 상위 단계
     * @param args
     */
    public static void main(String[] args) {
        ApplicationContext.run();

        // 현재는 스레드 강제 점유로 동작하는 것처럼 보이게 하기
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}
