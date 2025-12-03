package global.aop.log;

import java.lang.reflect.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogConfig {
    // 헬퍼 메서드
    public static Logger getLogger(Object bean) {
        try {
            Class<?> beanClass = bean.getClass();
            if (beanClass.getName().contains("ByteBuddy")) {
                beanClass = beanClass.getSuperclass(); // 원본 클래스
            }
            return LoggerFactory.getLogger(beanClass);
        } catch (Exception e) {
            throw new RuntimeException("Logger 획득 실패: " + bean.getClass().getSimpleName(), e);
        }
    }
}
