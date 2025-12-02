package global.log;

import java.lang.reflect.Field;
import org.slf4j.LoggerFactory;

public class LogConfig {
    public static void initializeLogger(Object bean) {
        Class<?> clazz = bean.getClass();

        boolean hasLogAnnotation = false;
        Class<?> searchClass = clazz;
        while (searchClass != null) {
            if (searchClass.isAnnotationPresent(Log.class)) {
                hasLogAnnotation = true;
                break;
            }
            searchClass = searchClass.getSuperclass();
        }

        if (!hasLogAnnotation)
            return;

        // log 필드 찾기
        Field logField = null;
        searchClass = clazz;
        while (searchClass != null) {
            try {
                logField = searchClass.getDeclaredField("log");
                break;
            } catch (NoSuchFieldException e) {
                searchClass = searchClass.getSuperclass();
            }
        }

        if (logField == null) {
            throw new RuntimeException("Logger 타입 필드 누락: " + clazz.getSimpleName());
        }

        try {
            logField.setAccessible(true);
            logField.set(bean, LoggerFactory.getLogger(clazz));
        } catch (Exception e) {
            throw new RuntimeException("Logger 주입 실패 : " + clazz.getSimpleName(), e);
        }
    }
}
