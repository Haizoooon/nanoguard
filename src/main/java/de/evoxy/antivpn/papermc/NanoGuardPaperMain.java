package de.evoxy.antivpn.papermc;

import de.evoxy.antivpn.NanoGuardMain;
import de.evoxy.antivpn.papermc.commands.NanoGuardCommand;
import de.evoxy.antivpn.config.DefaultConfig;
import de.evoxy.antivpn.papermc.listener.PlayerJoinListener;
import de.evoxy.easyjsonconfig.ConfigManager;

import de.evoxy.flux.sql.FluxSqlWrapper;
import de.evoxy.flux.stores.DataStore;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class NanoGuardPaperMain extends JavaPlugin {

    private static NanoGuardPaperMain instance;

    private  NanoGuardMain nanoGuardMain;

    @Override
    public void onEnable() {
        instance = this;

        nanoGuardMain = new NanoGuardMain();

        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), this);


        Objects.requireNonNull(getCommand("nanoguard")).setExecutor(new NanoGuardCommand());
        Objects.requireNonNull(getCommand("nanoguard")).setTabCompleter(new NanoGuardCommand());

    }

    public NanoGuardMain getNanoGuardMain() {
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
