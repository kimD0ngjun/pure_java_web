package global.aop.log;

import java.lang.annotation.*;

/**
 * Logger 자동 주입 어노테이션(Slf4j 흉내내기)
 */
@Retention(RetentionPolicy.RUNTIME) // 런타임에 리플렉션으로 확인
@Target(ElementType.METHOD) // 메소드 레벨에서 적용
public @interface Log {
}
