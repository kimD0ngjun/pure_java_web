package global.reader;

// 자바는 정적 언어이므로 중첩 클래스 기반으로 SnakeYML 생성자에 넣어주면 편함
// 그냥 Map으로 처리하면 너무 복잡...
public class AppYmlConfig {
    private ServerConfig server;
    private LogOptionConfig logging;
    private DatabaseInfoConfig database;

    public ServerConfig getServer() {
        return server;
    }

    public void setServer(ServerConfig server) {
        this.server = server;
    }

    public LogOptionConfig getLogging() {
        return logging;
    }

    public void setLogging(LogOptionConfig logging) {
        this.logging = logging;
    }

    public DatabaseInfoConfig getDatabase() {
        return database;
    }

    public void setDatabase(DatabaseInfoConfig database) {
        this.database = database;
    }

    public static class ServerConfig {
        private String host;
        private int port;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }

    public static class LogOptionConfig {
        private String level;

        public String getLevel() {
            return level;
        }

        public void setLevel(String level) {
            this.level = level;
        }
    }

    public static class DatabaseInfoConfig {
        private String url;
        private String username;
        private String password;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
