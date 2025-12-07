package test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import domain.A;
import domain.B;
import global.context.ApplicationContext;
import global.ioc.Bean;
import org.junit.jupiter.api.BeforeAll;

public class Test {
    private static ApplicationContext context;

    @BeforeAll
    static void setUp() {
        context = ApplicationContext.run(); // 컨텍스트 초기화
    }

    @org.junit.jupiter.api.Test
    void test1() {
        // 예시: B라는 빈이 등록되어 있는지 확인
        Bean b = context.getBean(B.class); // 구체 타입 사용 가능
        assertNotNull(b, "빈 B가 컨테이너에 등록되어 있어야 함");
    }

    @org.junit.jupiter.api.Test
    void test2() {
        A a = (A) context.getBean(A.class);
        a.testA();
    }
}