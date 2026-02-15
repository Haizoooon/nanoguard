package de.evoxy.antivpn.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.evoxy.antivpn.NanoGuardMain;
import de.evoxy.antivpn.config.DefaultConfig;
import de.evoxy.antivpn.database.BlockedAddressesTable;
import de.evoxy.antivpn.database.CaughtPlayersTable;
import de.evoxy.flux.query.Query;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class VpnChecker {

    private static final String API_URL = "https://proxycheck.io/v3/";
    private static final int TIMEOUT = 2000;

    public static CompletableFuture<Boolean> checkVpn(String ip, NanoPlayer nanoPlayer) {
        return CompletableFuture.supplyAsync(() -> {
            try {

                if(!new Query<>(BlockedAddressesTable.class, NanoGuardMain.getInstance().getDataStore()).where("address", ip).execute().isEmpty()){
                    return true;
                }

                JsonObject responseJson = fetchIpData(ip);
                System.out.println("API Response: " + responseJson);
                if (responseJson == null || !"ok".equalsIgnoreCase(responseJson.get("status").getAsString())) {
                    return false;
                }

                JsonObject ipData = responseJson.getAsJsonObject(ip);
                JsonObject detections = ipData.getAsJsonObject("detections");

                boolean flagged = detections.get("proxy").getAsBoolean() || detections.get("vpn").getAsBoolean();

                if (flagged) {
                    handleDatabaseEntry(ip, nanoPlayer.uniqueId(), ipData);
                }

                return flagged;
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return false;
            }
        });
    }

    private static JsonObject fetchIpData(String ip) throws Exception {
        DefaultConfig defaultConfig = NanoGuardMain.getInstance().getDefaultConfig();
        URL url = new URI(API_URL + ip + (Objects.equals(defaultConfig.api_key, "") ? "" : "?key=" + defaultConfig.api_key)).toURL();

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(TIMEOUT);

        try (InputStream is = conn.getInputStream(); Scanner scanner = new Scanner(is)) {
            return scanner.hasNext() ? JsonParser.parseString(scanner.useDelimiter("\\A").next()).getAsJsonObject() : null;
        }
    }

    private static void handleDatabaseEntry(String ip, UUID uuid, JsonObject ipData) {
        Optional<BlockedAddressesTable> existing = new Query<>(BlockedAddressesTable.class, NanoGuardMain.getInstance().getDataStore())
                .where("address", ip)
                .execute().stream().findFirst();

        String addressId = existing.map(table -> table.id).orElseGet(() -> saveNewBlockedAddress(ip, ipData));
        saveCaughtPlayer(addressId, uuid);
    }

    private static String saveNewBlockedAddress(String ip, JsonObject ipData) {
        String id = UUID.randomUUID().toString();
        JsonObject loc = ipData.getAsJsonObject("location");
        JsonObject det = ipData.getAsJsonObject("detections");

        JsonObject info = new JsonObject();
        info.addProperty("continent_code", loc.get("continent_code").getAsString());
        info.addProperty("country_name", loc.get("country_name").getAsString());
        info.addProperty("is_proxy", det.get("proxy").getAsBoolean());
        info.addProperty("is_vpn", det.get("vpn").getAsBoolean());
        info.addProperty("operator_name", ipData.get("operator").getAsJsonObject().get("name").getAsString());

        BlockedAddressesTable entry = new BlockedAddressesTable();
        entry.id = id;
        entry.address = ip;
        entry.timestamp = System.currentTimeMillis();
        entry.information = info.toString();

        NanoGuardMain.getInstance().getDataStore().save(entry);
        return id;
    }

    private static void saveCaughtPlayer(String addressId, UUID uuid) {
        CaughtPlayersTable log = new CaughtPlayersTable();
        log.bound_address_id = addressId;
        log.playerUniqueId = uuid.toString();
        log.timestamp = System.currentTimeMillis();
        NanoGuardMain.getInstance().getDataStore().save(log);
    }
}