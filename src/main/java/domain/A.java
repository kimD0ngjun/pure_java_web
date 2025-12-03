package domain;

import global.ioc.Bean;

public class A implements Bean {
    private final B b;

    public A(B b) {
        this.b = b;
    }
}
