package global.ioc;

import global.log.Log;

/**
 * IoC 컨테이너가 관리하는 빈 역할을 맡는다.
 * 빈을 추상화한 이유는, IocContainerConfig의 주요 역할 중 하나인 빈 싱글톤 생명주기 관리를 위해서
 * 빈이 추가돼도 동일한 관리 정책 적용을 위해서
 */
@Log
public interface Bean {
    void init();
    void destroy();
}
