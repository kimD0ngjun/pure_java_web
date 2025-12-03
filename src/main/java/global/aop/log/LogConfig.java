package global.aop.log;

import java.lang.reflect.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogConfig {
//    public static void initializeLogger(Object bean) {
//        Class<?> clazz = bean.getClass();
//
//        // 상위 클래스 반복 탐색
//        boolean hasLogAnnotation = false;
//        Class<?> searchClass = clazz;
//        while (searchClass != null) {
//            if (searchClass.isAnnotationPresent(Log.class)) {
//                hasLogAnnotation = true;
//                break;
//            }
//            searchClass = searchClass.getSuperclass();
//        }
//
//        // 어노테이션 있으면 패스
//        if (!hasLogAnnotation) return;
//
//        // 없으면 이제 log 필드 찾기, 필드 없으면 선언
//        Field logField = null;
//        searchClass = clazz;
//        while (searchClass != null) {
//            try {
//                logField = searchClass.getDeclaredField("log");
//                break;
//            } catch (NoSuchFieldException e) {
//                searchClass = searchClass.getSuperclass();
//            }
//        }
//
//        // 예외 처리
//        if (logField == null) {
//            throw new RuntimeException("Logger 타입 필드 누락: " + clazz.getSimpleName());
//        }
//
//        // 빈 필드에 로깅 팩토리 할당
//        try {
//            logField.setAccessible(true);
//            logField.set(bean, LoggerFactory.getLogger(clazz));
//        } catch (Exception e) {
//            throw new RuntimeException("Logger 주입 실패 : " + clazz.getSimpleName(), e);
//        }
//    }

    // 헬퍼 메서드
    public static Logger getLogger(Object bean) {
        try {
            Field logField = bean.getClass().getDeclaredField("log");
            logField.setAccessible(true);
            return (Logger) logField.get(bean);
        } catch (Exception e) {
            throw new RuntimeException("Logger 획득 실패: " + bean.getClass().getSimpleName(), e);
        }
    }
}
