package de.evoxy.antivpn.papermc.commands;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.evoxy.antivpn.papermc.NanoGuardPaperMain;
import de.evoxy.antivpn.config.DefaultConfig;
import de.evoxy.antivpn.database.BlockedAddressesTable;
import de.evoxy.antivpn.database.WhitelistedTable;
import de.evoxy.flux.query.Query;
import de.evoxy.flux.stores.DataStore;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class NanoGuardCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NonNull @NotNull String[] args) {

        if(sender instanceof Player player){
            DefaultConfig defaultConfig = NanoGuardPaperMain.getInstance().getDefaultConfig();

            MiniMessage message = MiniMessage.miniMessage();

            if(!player.hasPermission(defaultConfig.permissions.command)){
                player.sendMessage(message.deserialize(defaultConfig.messages.no_permissions));
                return true;
            }

            if(args.length == 0){
                player.sendMessage(message.deserialize(defaultConfig.prefix + " <rainbow>NanoGuard</rainbow> <#aaaaaa>Plugin by Haizon</#aaaaaa> <#5555ff>v1.0.0</#5555ff>"));
                player.sendMessage("");
                player.sendMessage(message.deserialize("<#555555>/<#aaaaaa>nanoguard notify <#5555ff>Activate/deactivate notifications"));
                player.sendMessage(message.deserialize("<#555555>/<#aaaaaa>nanoguard blocked <#5555ff>List all blocked IP addresses"));
                player.sendMessage("");
                return true;
            }

            if(args.length == 1){
                if(args[0].equalsIgnoreCase("notifications") || args[0].equalsIgnoreCase("notify")){
                    defaultConfig.notifyEnabled = !defaultConfig.notifyEnabled;
                    NanoGuardPaperMain.getInstance().getConfigConfigManager().save();
                    NanoGuardPaperMain.getInstance().getNanoGuardMain().setConfigConfigManager(NanoGuardPaperMain.getInstance().getConfigConfigManager());
                    String notifyStatus = defaultConfig.notifyEnabled ? defaultConfig.messages.notify_enabled : defaultConfig.messages.notify_disabled;
                    player.sendMessage(message.deserialize(defaultConfig.prefix + notifyStatus));
                    return true;
                } else if(args[0].equalsIgnoreCase("blocked")){

                    if(defaultConfig.database.enabled){
                        player.sendMessage(message.deserialize(defaultConfig.prefix + " <#5555ff>Blocked IP Addresses:</#5555ff>"));

                        DataStore dataStore = NanoGuardPaperMain.getInstance().getDataStore();
                        Query<BlockedAddressesTable> query = new Query<>(BlockedAddressesTable.class, dataStore);

                        query.execute().forEach(entry -> {

                            JsonObject jsonObject = JsonParser.parseString(entry.information).getAsJsonObject();

                            String hoverText = "<#555555>Information:\n" +
                                    "<gray>Continent Code</gray> " + "<red>" + jsonObject.get("continent_code").getAsString() + "</red>\n" +
                                    "<gray>Country Name</gray> " + "<red>" + jsonObject.get("country_name").getAsString() + "</red>\n" +
                                    "<gray>Proxy</gray> " + "<red>" + jsonObject.get("is_proxy").getAsBoolean() + "</red>\n" +
                                    "<gray>VPN</gray> " + "<red>" + jsonObject.get("is_vpn").getAsBoolean() + "</red>\n" +
                                    "<gray>Operator</gray> " + "<red>" + jsonObject.get("operator_name").getAsString() + "</red>";

                            String formattedDate = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm").format(java.time.Instant.ofEpochMilli(entry.timestamp).atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
                            player.sendMessage(message.deserialize("<hover:show_text:'" + hoverText + "'><b><blue>• </blue></b><#aaaaaa>" + entry.address + "</#aaaaaa> <#555555>—</#555555> <blue>" + formattedDate + "</blue></hover>"));
                        });

                    } else {
                        player.sendMessage(message.deserialize(defaultConfig.prefix + " <#ff5555>Database is disabled in the configuration.</#ff5555>"));
                    }

                    return true;
                }
            } else if(args.length == 2) {
                if (args[0].equalsIgnoreCase("whitelist")) {
                    if (args[1].equalsIgnoreCase("list")) {
                        DataStore dataStore = NanoGuardPaperMain.getInstance().getDataStore();
                        Query<WhitelistedTable> query = new Query<>(WhitelistedTable.class, dataStore);

                        player.sendMessage(message.deserialize(defaultConfig.prefix + " <#5555ff>Whitelisted Entries:</#5555ff>"));
                        query.execute().forEach(entry -> {
                            player.sendMessage(message.deserialize("<#aaaaaa>" + entry.type + "</#aaaaaa>: <blue>" + entry.value + "</blue>"));
                        });
                    }
                }
            } else if(args.length == 3) {
                if (args[0].equalsIgnoreCase("whitelist") && args[1].equalsIgnoreCase("remove")) {
                    DataStore dataStore = NanoGuardPaperMain.getInstance().getDataStore();
                    Query<WhitelistedTable> query = new Query<>(WhitelistedTable.class, dataStore).where("value", args[2]);
                    List<WhitelistedTable> entries = query.execute();

                    if (entries.isEmpty()) {
                        player.sendMessage(message.deserialize(defaultConfig.prefix + " <#ff5555>No whitelist entry found for value: " + args[2] + "</#ff5555>"));
                    } else {
                        query.delete();
                        player.sendMessage(message.deserialize(defaultConfig.prefix + " <#55ff55>Whitelist entry removed for value: " + args[2] + "</#55ff55>"));
                    }

                } else if(args[0].equalsIgnoreCase("whitelist") && args[1].equalsIgnoreCase("add")) {
                    String type = "NAME";
                    if(args[2].matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")){
                        type = "UUID";
                    } else if(args[2].matches("^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.(?!$)|$){4}$")){
                        type = "IP";
                    }

                    WhitelistedTable entry = new WhitelistedTable();
                    entry.type = type;
                    entry.value = args[2];

                    NanoGuardPaperMain.getInstance().getDataStore().save(entry);
                    player.sendMessage(message.deserialize(defaultConfig.prefix + " <#55ff55>Whitelist entry added for value: " + args[2] + "</#55ff55>"));
                }
            }

        }

        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NonNull @NotNull String[] args) {

        if(args.length > 0){
            if(args.length == 1){
                return List.of("notify", "blocked", "whitelist");
            } else if(args.length == 2){
                if(args[0].equalsIgnoreCase("whitelist")){
                    return List.of("add", "remove", "list");
                }
            } else if(args.length == 3) {
                if (args[0].equalsIgnoreCase("whitelist") && args[1].equalsIgnoreCase("remove")) {
                    DataStore dataStore = NanoGuardPaperMain.getInstance().getDataStore();
                    Query<WhitelistedTable> query = new Query<>(WhitelistedTable.class, dataStore);
                    return query.execute().stream().map(entry -> entry.value).toList();
                }
            }
        }

        return List.of();
    }
}
