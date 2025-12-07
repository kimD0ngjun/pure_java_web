package domain;

import global.aop.log.Log;
import global.ioc.Bean;

public class B implements Bean {
    @Log
    public void testB() {
        System.out.println("1부터 100까지의 합은?");
        int sum = 0;
        for (int i = 1; i <= 100; i++) {
            sum += i;
        }
        System.out.println("정답은 " + sum);
    }
}
