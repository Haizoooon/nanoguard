package de.evoxy.antivpn.velocity.listener;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.configuration.PlayerConfigurationEvent;
import de.evoxy.antivpn.NanoGuardMain;
import de.evoxy.antivpn.api.NanoPlayer;
import de.evoxy.antivpn.api.VpnChecker;
import de.evoxy.antivpn.database.BlockedAddressesTable;
import de.evoxy.antivpn.database.WhitelistedTable;
import de.evoxy.antivpn.velocity.NanoGuardVelocityMain;
import de.evoxy.flux.query.Query;
import de.evoxy.flux.stores.DataStore;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.List;
import java.util.Objects;

public class PlayerLogin {

    @Subscribe
    public void handle(PlayerConfigurationEvent event){

        String address = event.player().getRemoteAddress().getAddress().getHostAddress();
        NanoPlayer nanoPlayer = new NanoPlayer(event.player().getUniqueId(), event.player().getUsername());

        MiniMessage miniMessage = MiniMessage.miniMessage();

        if(event.player().hasPermission(NanoGuardMain.getInstance().getDefaultConfig().permissions.bypass)) return;

        DataStore dataStore = NanoGuardMain.getInstance().getDataStore();
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

        event.player().disconnect(miniMessage.deserialize(NanoGuardMain.getInstance().getDefaultConfig().messages.kick_message));

        if(NanoGuardMain.getInstance().getDefaultConfig().notifyEnabled){
            String message = NanoGuardMain.getInstance().getDefaultConfig().messages.notify_message.replace("%player%", Objects.requireNonNull(nanoPlayer.name())).replace("%prefix%", NanoGuardMain.getInstance().getDefaultConfig().prefix);

            JsonObject jsonObject = JsonParser.parseString(query.execute().getFirst().information).getAsJsonObject();

            String hoverText = "<#555555>Information:\n" +
                    "<gray>IP Address</gray> " + "<red>" + address + "</red>\n" +
                    "<gray>Continent Code</gray> " + "<red>" + jsonObject.get("continent_code").getAsString() + "</red>\n" +
                    "<gray>Country Name</gray> " + "<red>" + jsonObject.get("country_name").getAsString() + "</red>\n" +
                    "<gray>Proxy</gray> " + "<red>" + jsonObject.get("is_proxy").getAsBoolean() + "</red>\n" +
                    "<gray>VPN</gray> " + "<red>" + jsonObject.get("is_vpn").getAsBoolean() + "</red>\n" +
                    "<gray>Operator</gray> " + "<red>" + jsonObject.get("operator_name").getAsString() + "</red>";

            NanoGuardVelocityMain.getInstance().getProxyServer().getAllPlayers().forEach(player -> {
                if(player.hasPermission(NanoGuardMain.getInstance().getDefaultConfig().permissions.notify)){
                    player.sendMessage(miniMessage.deserialize("<hover:show_text:'" + hoverText + "'>" + message + "</hover>"));
                }
            });

        }

    }

}
