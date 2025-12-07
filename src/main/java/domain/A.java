package domain;

import global.aop.log.Log;

public class A implements ABean {
    private final B b;

    public A(B b) {
        this.b = b;
    }

    @Log
    @Override
    public void test() {
        System.out.println("테스트해봤슴");
    }
}
