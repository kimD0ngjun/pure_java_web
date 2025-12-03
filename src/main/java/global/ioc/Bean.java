package global.ioc;

/**
 * IoC 컨테이너가 관리하는 빈 역할을 맡는다.
 * 빈을 추상화한 이유는, IocContainerConfig의 주요 역할 중 하나인 빈 싱글톤 생명주기 관리를 위해서
 * 빈이 추가돼도 동일한 관리 정책 적용을 위해서
 *
 * 스프링에서의 빈 init은 빈의 생성을 담당하지 않는다
 * 객체 생성은 IoC 컨테이너가 new 혹은 팩토리 메소드로 빈 인스턴스를 생성
 * 이 과정이 필드 초기화 + 생성자 로직 실행이다
 *
 * 빈의 초기화란, 빈이 생성되고 의존성이 모두 주입된 후에 추가 로직(로깅, 캐싱 정리 등)을 수행하는 것
 * 즉, 빈 자체의 생성자를 AbstractBean에 종속시키는 것은 옳지 않음
 */
public interface Bean {
    default void init() {};
    default void destroy() {};
}
