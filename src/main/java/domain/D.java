package domain;

import global.ioc.Bean;

public class D implements Bean {
    private final B b;
    private final C c;

    public D(B b, C c) {
        this.b = b;
        this.c = c;
    }
}
