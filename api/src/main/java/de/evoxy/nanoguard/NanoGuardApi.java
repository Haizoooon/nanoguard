package de.evoxy.nanoguard;

import de.evoxy.easyjsonconfig.ConfigManager;
import de.evoxy.easyjsonconfig.exception.ConfigException;
import de.evoxy.flux.sql.FluxSqlWrapper;
import de.evoxy.flux.stores.DataStore;
import de.evoxy.nanoguard.config.DefaultConfig;
import de.evoxy.nanoguard.database.BlockedAddressesTable;
import de.evoxy.nanoguard.database.CaughtPlayersTable;
import de.evoxy.nanoguard.database.WhitelistedTable;

public class NanoGuardApi {

    private static NanoGuardApi instance;

    private ConfigManager<DefaultConfig> configConfigManager;

    private FluxSqlWrapper wrapper;
    private DataStore dataStore;

    public NanoGuardApi() {
        instance = this;

        configConfigManager= new ConfigManager<>("plugins/NanoGuard/config.json", DefaultConfig.class);
        configConfigManager.init("default_config.json");

        DefaultConfig.Database database = getDefaultConfig().database;

        if(database.enabled){
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (NoClassDefFoundError e){
                System.err.println("MySQL JDBC Driver not found. Please include the MySQL Connector/J library in your plugin's dependencies.");
                throw new ConfigException(e.getMessage());
            } catch (ClassNotFoundException e) {
                System.err.println("MySQL JDBC Driver class not found. Please ensure the MySQL Connector/J library is included in your plugin's dependencies.");
                throw new ConfigException(e.getMessage());
            }
            wrapper = new FluxSqlWrapper().configuration(database.host, database.database, database.username, database.password, database.port).connect();
            dataStore = new DataStore(wrapper);

            dataStore.ensureIndex(BlockedAddressesTable.class);
            dataStore.ensureIndex(CaughtPlayersTable.class);
            dataStore.ensureIndex(WhitelistedTable.class);
            System.out.println("Database enabled and connected successfully.");
        } else {
            System.out.println("Database is disabled in the configuration.");
        }

    }

    public FluxSqlWrapper getWrapper() {
        return wrapper;
    }

    public DataStore getDataStore() {
        return dataStore;
    }

    public void setConfigConfigManager(ConfigManager<DefaultConfig> configConfigManager) {
        this.configConfigManager = configConfigManager;
    }

    public ConfigManager<DefaultConfig> getConfigConfigManager() {
        return configConfigManager;
    }

    public DefaultConfig getDefaultConfig() {
        return configConfigManager.get();
    }

    public static NanoGuardApi getInstance() {
        return instance;
    }

}
