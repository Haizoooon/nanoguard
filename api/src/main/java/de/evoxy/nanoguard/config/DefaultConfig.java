package de.evoxy.nanoguard.config;

public class DefaultConfig {

    public String prefix;

    public boolean notifyEnabled;

    public String api_key;

    public RestServer restServer;
    public Messages messages;
    public Permissions permissions;
    public Database database;
    public Discord discord;

    public static class RestServer {
        public boolean enabled;
        public int port;
    }

    public static class Database {
        public boolean enabled;
        public String host;
        public int port;
        public String database;
        public String username;
        public String password;
    }

    public static class Messages {
        public String no_permissions;
        public String kick_message;

        public String notify_enabled;
        public String notify_disabled;

        public String notify_message;
    }

    public static class Permissions {
        public String bypass;
        public String notify;
        public String command;
    }

    public static class Discord {
        public boolean webhook_enabled;
        public String webhook_url;
    }

}
