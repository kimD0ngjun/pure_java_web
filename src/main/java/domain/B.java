package domain;

import global.ioc.Bean;

public class B implements Bean {
    private final A a;

    public B(A a) {
        this.a = a;
    }
}
