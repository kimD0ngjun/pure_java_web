package domain;

import global.ioc.AbstractBean;

public class D extends AbstractBean {
    private final B b;
    private final C c;

    public D(B b, C c) {
        this.b = b;
        this.c = c;
    }
}
