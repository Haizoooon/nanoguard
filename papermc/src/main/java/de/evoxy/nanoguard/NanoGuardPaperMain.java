package de.evoxy.nanoguard;


import de.evoxy.easyjsonconfig.ConfigManager;
import de.evoxy.flux.sql.FluxSqlWrapper;
import de.evoxy.flux.stores.DataStore;
import de.evoxy.nanoguard.commands.NanoGuardCommand;
import de.evoxy.nanoguard.config.DefaultConfig;
import de.evoxy.nanoguard.listener.PlayerJoinListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class NanoGuardPaperMain extends JavaPlugin {

    private static NanoGuardPaperMain instance;

    private  NanoGuardApi nanoGuardMain;

    @Override
    public void onEnable() {
        instance = this;

        nanoGuardMain = new NanoGuardApi();

        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), this);

        Objects.requireNonNull(getCommand("nanoguard")).setExecutor(new NanoGuardCommand());
        Objects.requireNonNull(getCommand("nanoguard")).setTabCompleter(new NanoGuardCommand());

    }

    public NanoGuardApi getNanoGuardMain() {
        return nanoGuardMain;
    }

    public FluxSqlWrapper getWrapper() {
        return nanoGuardMain.getWrapper();
    }

    public DataStore getDataStore() {
        return nanoGuardMain.getDataStore();
    }

    public ConfigManager<DefaultConfig> getConfigConfigManager() {
        return nanoGuardMain.getConfigConfigManager();
    }

    public DefaultConfig getDefaultConfig() {
        return nanoGuardMain.getConfigConfigManager().get();
    }

    public static NanoGuardPaperMain getInstance() {
        return instance;
    }
}
