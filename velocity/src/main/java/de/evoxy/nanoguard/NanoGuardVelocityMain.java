package de.evoxy.nanoguard;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import de.evoxy.nanoguard.commands.NanoGuardCommand;
import de.evoxy.nanoguard.listener.PlayerLogin;
import org.slf4j.Logger;

@Plugin(id = "nanoguard-velocity", name = "NanoGuard", version = "1.0.0", description = "A simple and efficient anti-VPN plugin for Velocity", authors = {"Haizon"})
public class NanoGuardVelocityMain {

    private static NanoGuardVelocityMain instance;

    private final ProxyServer proxyServer;
    private final Logger logger;

    private final NanoGuardApi nanoGuardMain;


    @Inject
    public NanoGuardVelocityMain(ProxyServer proxyServer, Logger logger){
        this.proxyServer = proxyServer;
        this.logger = logger;

        instance = this;

         this.nanoGuardMain = new NanoGuardApi();

        logger.info("NanoGuard for Velocity has been enabled!");

    }

    @Subscribe
    public void onInitialize(ProxyInitializeEvent event){
        proxyServer.getEventManager().register(this, new PlayerLogin());
        proxyServer.getCommandManager().register(proxyServer.getCommandManager().metaBuilder("nanoguard").build(), new NanoGuardCommand());
    }

    public static NanoGuardVelocityMain getInstance() {
        return instance;
    }

    public NanoGuardApi getNanoGuardMain() {
        return nanoGuardMain;
    }

    public ProxyServer getProxyServer() {
        return proxyServer;
    }

    public Logger getLogger() {
        return logger;
    }
}
