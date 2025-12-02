package domain;

import global.ioc.AbstractBean;

public class B extends AbstractBean {
    private final A a;

    public B(A a) {
        this.a = a;
    }
}
