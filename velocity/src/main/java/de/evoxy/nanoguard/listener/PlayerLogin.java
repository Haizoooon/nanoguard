package de.evoxy.nanoguard.listener;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.configuration.PlayerConfigurationEvent;
import de.evoxy.flux.query.Query;
import de.evoxy.flux.stores.DataStore;
import de.evoxy.nanoguard.NanoGuardApi;
import de.evoxy.nanoguard.NanoGuardVelocityMain;
import de.evoxy.nanoguard.check.VpnChecker;
import de.evoxy.nanoguard.database.BlockedAddressesTable;
import de.evoxy.nanoguard.database.WhitelistedTable;
import de.evoxy.nanoguard.player.NanoPlayer;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.List;
import java.util.Objects;

public class PlayerLogin {

    @Subscribe
    public void handle(PlayerConfigurationEvent event){

        String address = event.player().getRemoteAddress().getAddress().getHostAddress();
        NanoPlayer nanoPlayer = new NanoPlayer(event.player().getUniqueId(), event.player().getUsername());

        MiniMessage miniMessage = MiniMessage.miniMessage();

        if(event.player().hasPermission(NanoGuardApi.getInstance().getDefaultConfig().permissions.bypass)) return;

        DataStore dataStore = NanoGuardApi.getInstance().getDataStore();
        Query<BlockedAddressesTable> query = new Query<>(BlockedAddressesTable.class, dataStore).where("address", address);

        List<WhitelistedTable> whitelistedTable = new Query<>(WhitelistedTable.class, dataStore).execute();

        boolean isWhitelisted = whitelistedTable.stream().anyMatch(table -> switch (table.type.toUpperCase()) {
            case "IP" -> table.value.equalsIgnoreCase(address);
            case "UUID" -> table.value.equalsIgnoreCase(Objects.requireNonNull(nanoPlayer.uniqueId()).toString());
            case "NAME" -> table.value.equalsIgnoreCase(nanoPlayer.name());
            default -> {
                NanoGuardVelocityMain.getInstance().getLogger().warn("Unknown Whitelist-Type: {}", table.type);
                yield false;
            }
        });

        if(isWhitelisted) return;

        boolean flagged = VpnChecker.checkVpn(address, nanoPlayer).join().flagged();

        if(!flagged) return;

        event.player().disconnect(miniMessage.deserialize(NanoGuardApi.getInstance().getDefaultConfig().messages.kick_message));

        if(NanoGuardApi.getInstance().getDefaultConfig().notifyEnabled){
            String message = NanoGuardApi.getInstance().getDefaultConfig().messages.notify_message.replace("%player%", Objects.requireNonNull(nanoPlayer.name())).replace("%prefix%", NanoGuardApi.getInstance().getDefaultConfig().prefix);

            JsonObject jsonObject = JsonParser.parseString(query.execute().getFirst().information).getAsJsonObject();

            String hoverText = "<#555555>Information:\n" +
                    "<gray>IP Address</gray> " + "<red>" + address + "</red>\n" +
                    "<gray>Continent Code</gray> " + "<red>" + jsonObject.get("continent_code").getAsString() + "</red>\n" +
                    "<gray>Country Name</gray> " + "<red>" + jsonObject.get("country_name").getAsString() + "</red>\n" +
                    "<gray>Proxy</gray> " + "<red>" + jsonObject.get("is_proxy").getAsBoolean() + "</red>\n" +
                    "<gray>VPN</gray> " + "<red>" + jsonObject.get("is_vpn").getAsBoolean() + "</red>\n" +
                    "<gray>Operator</gray> " + "<red>" + jsonObject.get("operator_name").getAsString() + "</red>";

            NanoGuardVelocityMain.getInstance().getProxyServer().getAllPlayers().forEach(player -> {
                if(player.hasPermission(NanoGuardApi.getInstance().getDefaultConfig().permissions.notify)){
                    player.sendMessage(miniMessage.deserialize("<hover:show_text:'" + hoverText + "'>" + message + "</hover>"));
                }
            });

        }

    }

}
