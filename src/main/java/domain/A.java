package domain;

import global.aop.log.Log;
import global.ioc.Bean;

public class A implements Bean {
    private final B b;

    public A(B b) {
        this.b = b;
    }

    @Log
    public void test() {
        System.out.println("테스트해봤슴");
    }
}
