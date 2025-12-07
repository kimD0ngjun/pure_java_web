package domain;

public class D implements DBean {
    private final B b;
    private final C c;

    public D(B b, C c) {
        this.b = b;
        this.c = c;
    }
}
