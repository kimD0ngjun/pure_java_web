package global.log;

import java.lang.reflect.Field;
import org.slf4j.LoggerFactory;

public class LogConfig {
    public static void initializeLogger(Object bean) {
        Class<?> clazz = bean.getClass();

        if (!clazz.isAnnotationPresent(Log.class)) return;

        try {
            // log 필드 자동 생성
            Field logField = clazz.getDeclaredField("log");
            logField.setAccessible(true);
            logField.set(bean, LoggerFactory.getLogger(clazz));
        } catch (Exception e) {
            throw new RuntimeException("Logger 주입 실패 : " + clazz.getSimpleName(), e);
        }
    }
}
