package global.reader;

import java.io.IOException;
import java.io.InputStream;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public class YmlConfigReader {

    private final AppYmlConfig config;

    // 설정파일 경로를 기반으로 읽기 시작
    public YmlConfigReader(String configPath) {
        // 2버전 이상부터는 직접 로더옵션 생성자에 넣어줘야함
        Yaml yml = new Yaml(new Constructor(AppYmlConfig.class, new LoaderOptions()));

        try (InputStream is = getClass().getClassLoader().getResourceAsStream(configPath)) {
            if (is == null)
                throw new RuntimeException("yml 설정파일 경로 읽기 실패: " + configPath);
            this.config = yml.load(is);
        } catch (IOException e) {
            throw new RuntimeException("yml 설정파일 로딩 실패: " + e.getMessage());
        }
    }

    // 초기화된 설정 변수를 getter로 갖고와 밖에서 써먹기
    public AppYmlConfig getConfig() {
        return config;
    }
}
