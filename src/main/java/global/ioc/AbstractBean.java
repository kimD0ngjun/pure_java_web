package global.ioc;

import global.log.LogConfig;
import org.slf4j.Logger;

/**
 * 스프링에서의 빈 init은 빈의 생성을 담당하지 않는다
 * 객체 생성은 IoC 컨테이너가 new 혹은 팩토리 메소드로 빈 인스턴스를 생성
 * 이 과정이 필드 초기화 + 생성자 로직 실행이다
 *
 * 빈의 초기화란, 빈이 생성되고 의존성이 모두 주입된 후에 추가 로직(로깅, 캐싱 정리 등)을 수행하는 것
 * 즉, 빈 자체의 생성자를 AbstractBean에 종속시키는 것은 옳지 않음
 */
public abstract class AbstractBean implements Bean {
    // 완벽한 흉내는 아니지만, Log 어노테이션을 통한 약간의 모방
    protected Logger log;

    @Override
    public void init() {
        // IocContainerConfig가 LogConfig 주입하지 않아도 자동 처리
        if (log == null) LogConfig.initializeLogger(this);
        log.info("{} 빈 초기화", this.getClass().getSimpleName());
    }

    @Override
    public void destroy() {
        // 가능하다면 내부 리소스 해제 모방으로 공통 필드 null 할당 같은 것도 가능할듯
        log.info("{} 빈 종료", this.getClass().getSimpleName());
        log = null; // 로그 null 할당(리소스 정리)
    }
}
