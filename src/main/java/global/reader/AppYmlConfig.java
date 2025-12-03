package global.reader;

public class AppYmlConfig {
    private ServerConfig server;
    private LogOptionConfig log;
    private DatabaseInfoConfig db;

    public ServerConfig getServer() {
        return server;
    }

    public void setServer(ServerConfig server) {
        this.server = server;
    }

    public LogOptionConfig getLog() {
        return log;
    }

    public void setLog(LogOptionConfig log) {
        this.log = log;
    }

    public DatabaseInfoConfig getDb() {
        return db;
    }

    public void setDb(DatabaseInfoConfig db) {
        this.db = db;
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
