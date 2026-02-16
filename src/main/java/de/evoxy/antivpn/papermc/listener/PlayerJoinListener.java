package de.evoxy.antivpn.papermc.listener;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.evoxy.antivpn.api.NanoPlayer;
import de.evoxy.antivpn.api.VpnResult;
import de.evoxy.antivpn.papermc.NanoGuardPaperMain;
import de.evoxy.antivpn.api.VpnChecker;
import de.evoxy.antivpn.database.BlockedAddressesTable;
import de.evoxy.antivpn.database.WhitelistedTable;
import de.evoxy.flux.query.Query;
import de.evoxy.flux.stores.DataStore;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.List;
import java.util.Objects;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void handle(AsyncPlayerPreLoginEvent event){
        String ipAddress = event.getAddress().getHostAddress();
        PlayerProfile profile = event.getPlayerProfile();

        MiniMessage miniMessage = MiniMessage.miniMessage();

        DataStore dataStore = NanoGuardPaperMain.getInstance().getDataStore();
        Query<BlockedAddressesTable> query = new Query<>(BlockedAddressesTable.class, dataStore).where("address", ipAddress);

        List<WhitelistedTable> whitelistedTable = new Query<>(WhitelistedTable.class, dataStore).execute();

        boolean isWhitelisted = whitelistedTable.stream().anyMatch(table -> switch (table.type.toUpperCase()) {
            case "IP" -> table.value.equalsIgnoreCase(ipAddress);
            case "UUID" -> table.value.equalsIgnoreCase(Objects.requireNonNull(profile.getId()).toString());
            case "NAME" -> table.value.equalsIgnoreCase(profile.getName());
            default -> {
                NanoGuardPaperMain.getInstance().getLogger().warning("Unbekannter Whitelist-Typ: " + table.type);
                yield false;
            }
        });

        if(isWhitelisted) return;

        NanoPlayer nanoPlayer = new NanoPlayer(profile.getId(), profile.getName());

        boolean flagged = VpnChecker.checkVpn(ipAddress, nanoPlayer).join().flagged();

        if(!flagged) return;

        event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
        event.kickMessage(miniMessage.deserialize(NanoGuardPaperMain.getInstance().getDefaultConfig().messages.kick_message));

        if(NanoGuardPaperMain.getInstance().getDefaultConfig().notifyEnabled){
            String message = NanoGuardPaperMain.getInstance().getDefaultConfig().messages.notify_message.replace("%player%", Objects.requireNonNull(profile.getName())).replace("%prefix%", NanoGuardPaperMain.getInstance().getDefaultConfig().prefix);

            JsonObject jsonObject = JsonParser.parseString(query.execute().getFirst().information).getAsJsonObject();

            String hoverText = "<#555555>Information:\n" +
                    "<gray>IP Address</gray> " + "<red>" + ipAddress + "</red>\n" +
                    "<gray>Continent Code</gray> " + "<red>" + jsonObject.get("continent_code").getAsString() + "</red>\n" +
                    "<gray>Country Name</gray> " + "<red>" + jsonObject.get("country_name").getAsString() + "</red>\n" +
                    "<gray>Proxy</gray> " + "<red>" + jsonObject.get("is_proxy").getAsBoolean() + "</red>\n" +
                    "<gray>VPN</gray> " + "<red>" + jsonObject.get("is_vpn").getAsBoolean() + "</red>\n" +
                    "<gray>Operator</gray> " + "<red>" + jsonObject.get("operator_name").getAsString() + "</red>";

            Bukkit.getOnlinePlayers().forEach(player -> {
                player.sendMessage(miniMessage.deserialize("<hover:show_text:'" + hoverText + "'>" + message + "</hover>"));
            });
        }

    }

}
