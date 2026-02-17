package de.evoxy.nanoguard.commands;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import de.evoxy.flux.query.Query;
import de.evoxy.flux.stores.DataStore;
import de.evoxy.nanoguard.NanoGuardApi;
import de.evoxy.nanoguard.config.DefaultConfig;
import de.evoxy.nanoguard.database.BlockedAddressesTable;
import de.evoxy.nanoguard.database.WhitelistedTable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class NanoGuardCommand implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {

        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        MiniMessage message = MiniMessage.miniMessage();
        DefaultConfig defaultConfig = NanoGuardApi.getInstance().getDefaultConfig();

        if(args.length == 0){
            source.sendMessage(message.deserialize(defaultConfig.prefix + " <rainbow>NanoGuard</rainbow> <#aaaaaa>Plugin by Haizon</#aaaaaa> <#5555ff>v1.0.0</#5555ff>"));
            source.sendMessage(Component.text());
            source.sendMessage(message.deserialize("<#555555>/<#aaaaaa>nanoguard notify <#5555ff>Activate/deactivate notifications"));
            source.sendMessage(message.deserialize("<#555555>/<#aaaaaa>nanoguard reload <#5555ff>Reload the configuration"));
            source.sendMessage(message.deserialize("<#555555>/<#aaaaaa>nanoguard blocked <#5555ff>List all blocked IP addresses"));
            source.sendMessage(message.deserialize("<#555555>/<#aaaaaa>nanoguard whitelist <list/add/remove> <#5555ff>Whitelist an IP address, UUID or player name"));
            source.sendMessage(Component.text());
            return;
        }

        if(args.length == 1){
            if(args[0].equalsIgnoreCase("notifications") || args[0].equalsIgnoreCase("notify")){
                defaultConfig.notifyEnabled = !defaultConfig.notifyEnabled;
                NanoGuardApi.getInstance().getConfigConfigManager().save();
                NanoGuardApi.getInstance().setConfigConfigManager(NanoGuardApi.getInstance().getConfigConfigManager());
                String notifyStatus = defaultConfig.notifyEnabled ? defaultConfig.messages.notify_enabled : defaultConfig.messages.notify_disabled;
                source.sendMessage(message.deserialize(defaultConfig.prefix + notifyStatus));
            } else if(args[0].equalsIgnoreCase("blocked")){
                if(defaultConfig.database.enabled){
                    source.sendMessage(message.deserialize(defaultConfig.prefix + " <#5555ff>Blocked IP Addresses:</#5555ff>"));

                    List<BlockedAddressesTable> blockedAddresses = new Query<>(BlockedAddressesTable.class, NanoGuardApi.getInstance().getDataStore()).execute();

                    if(blockedAddresses.isEmpty()){
                        source.sendMessage(message.deserialize(defaultConfig.prefix + " <#aaaaaa>No blocked IP addresses found.</#aaaaaa>"));
                        return;
                    }

                    blockedAddresses.forEach(entry -> {

                        JsonObject jsonObject = JsonParser.parseString(entry.information).getAsJsonObject();

                        String hoverText = "<#555555>Information:\n" +
                                "<gray>Continent Code</gray> " + "<red>" + jsonObject.get("continent_code").getAsString() + "</red>\n" +
                                "<gray>Country Name</gray> " + "<red>" + jsonObject.get("country_name").getAsString() + "</red>\n" +
                                "<gray>Proxy</gray> " + "<red>" + jsonObject.get("is_proxy").getAsBoolean() + "</red>\n" +
                                "<gray>VPN</gray> " + "<red>" + jsonObject.get("is_vpn").getAsBoolean() + "</red>\n" +
                                "<gray>Operator</gray> " + "<red>" + jsonObject.get("operator_name").getAsString() + "</red>";

                        String formattedDate = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm").format(java.time.Instant.ofEpochMilli(entry.timestamp).atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
                        source.sendMessage(message.deserialize("<hover:show_text:'" + hoverText + "'><b><blue>• </blue></b><#aaaaaa>" + entry.address + "</#aaaaaa> <#555555>—</#555555> <blue>" + formattedDate + "</blue></hover>"));
                    });
                } else {
                    source.sendMessage(message.deserialize(defaultConfig.prefix + " <#ff5555>Database is disabled. Cannot retrieve blocked IP addresses.</#ff5555>"));
                }
                return;
            } else if(args[0].equalsIgnoreCase("reload")){
                NanoGuardApi.getInstance().setConfigConfigManager(NanoGuardApi.getInstance().getConfigConfigManager());
            }
        } else if(args.length == 2){
            if (args[0].equalsIgnoreCase("whitelist")) {
                if(args[1].equalsIgnoreCase("list")){
                    invocation.source().sendMessage(message.deserialize(defaultConfig.prefix + " <#5555ff>Whitelisted Entries:</#5555ff>"));
                    new Query<>(WhitelistedTable.class, NanoGuardApi.getInstance().getDataStore()).execute().forEach(entry -> {
                        invocation.source().sendMessage(message.deserialize("<#aaaaaa>" + entry.type + "</#aaaaaa>: <blue>" + entry.value + "</blue>"));
                    });
                }
            }
        } else if(args.length == 3){
            if (args[0].equalsIgnoreCase("whitelist") && args[1].equalsIgnoreCase("remove")) {
                DataStore dataStore = NanoGuardApi.getInstance().getDataStore();
                Query<WhitelistedTable> query = new Query<>(WhitelistedTable.class, dataStore).where("value", args[2]);
                List<WhitelistedTable> entries = query.execute();

                if (entries.isEmpty()) {
                    invocation.source().sendMessage(message.deserialize(defaultConfig.prefix + " <#ff5555>No whitelist entry found for value: " + args[2] + "</#ff5555>"));
                } else {
                    query.delete();
                    invocation.source().sendMessage(message.deserialize(defaultConfig.prefix + " <#55ff55>Whitelist entry removed for value: " + args[2] + "</#55ff55>"));
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

                NanoGuardApi.getInstance().getDataStore().save(entry);
                invocation.source().sendMessage(message.deserialize(defaultConfig.prefix + " <#55ff55>Whitelist entry added for value: " + args[2] + "</#55ff55>"));
            }
        }

    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        List<String> suggestions = new ArrayList<>();

        if(args.length == 1){
            suggestions.add("notify");
            suggestions.add("blocked");
            suggestions.add("whitelist");
            suggestions.add("reload");
        } else if(args.length == 2 && args[0].equalsIgnoreCase("whitelist")){
            suggestions.add("list");
            suggestions.add("add");
            suggestions.add("remove");
        } else if(args.length == 3 && args[0].equalsIgnoreCase("whitelist") && args[1].equalsIgnoreCase("remove")){
            new Query<>(WhitelistedTable.class, NanoGuardApi.getInstance().getDataStore()).execute().forEach(entry -> suggestions.add(entry.value));
        }

        return suggestions;
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission(NanoGuardApi.getInstance().getDefaultConfig().permissions.command);
    }
}
