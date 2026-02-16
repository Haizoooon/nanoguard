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

    public static CompletableFuture<VpnResult> checkVpn(String ip, NanoPlayer nanoPlayer) {
        return CompletableFuture.supplyAsync(() -> {
            try {

                if(!new Query<>(BlockedAddressesTable.class, NanoGuardMain.getInstance().getDataStore()).where("address", ip).execute().isEmpty()){
                    return nullResult();
                }

                JsonObject responseJson = fetchIpData(ip);
                System.out.println("API Response: " + responseJson);
                if (responseJson == null || !"ok".equalsIgnoreCase(responseJson.get("status").getAsString())) {
                    return nullResult();
                }

                JsonObject ipData = responseJson.getAsJsonObject(ip);
                JsonObject detections = ipData.getAsJsonObject("detections");

                boolean flagged = detections.get("proxy").getAsBoolean() || detections.get("vpn").getAsBoolean() || detections.get("tor").getAsBoolean();

                if (flagged) {
                    handleDatabaseEntry(ip, nanoPlayer.uniqueId(), ipData);
                }

                //TODO: remove second save block.
                return saveNewBlockedAddress(ip, responseJson);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return null;
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

        String addressId = existing.map(table -> table.id).orElseGet(() -> saveNewBlockedAddress(ip, ipData).vpnResultId());
        saveCaughtPlayer(addressId, uuid);
    }

    private static VpnResult saveNewBlockedAddress(String ip, JsonObject ipData) {
        String id = UUID.randomUUID().toString();
        JsonObject loc = ipData.getAsJsonObject("location");
        JsonObject det = ipData.getAsJsonObject("detections");

        String continentName, continentCode, countryName, countryCode;
        boolean isProxy, isVpn, isTor, flagged;
        int risk;

        continentName = loc.get("continent_name").getAsString();
        continentCode = loc.get("continent_code").getAsString();
        countryName = loc.get("country_name").getAsString();
        countryCode = loc.get("country_code").getAsString();

        isProxy = det.get("proxy").getAsBoolean();
        isVpn = det.get("vpn").getAsBoolean();
        isTor = det.get("tor").getAsBoolean();

        risk = det.get("risk").getAsInt();

        JsonObject info = new JsonObject();
        info.addProperty("continent_name", continentName);
        info.addProperty("continent_code", continentCode);
        info.addProperty("country_name", countryName);
        info.addProperty("country_code", countryCode);
        info.addProperty("is_proxy", isProxy);
        info.addProperty("is_vpn", isVpn);
        info.addProperty("is_tor", isTor);
        info.addProperty("risk", risk);
        info.addProperty("operator_name", ipData.get("operator").getAsJsonObject().get("name").getAsString());

        flagged = isVpn || isTor || isProxy;

        BlockedAddressesTable entry = new BlockedAddressesTable();
        entry.id = id;
        entry.address = ip;
        entry.timestamp = System.currentTimeMillis();
        entry.information = info.toString();

        NanoGuardMain.getInstance().getDataStore().save(entry);
        return new VpnResult(id, continentName, continentCode, countryName, countryCode, isProxy, isVpn, isTor, flagged, risk);
    }

    private static VpnResult nullResult(){
        return new VpnResult("", "", "", "", "", false, false, false, false, 0);
    }
    
    private static void saveCaughtPlayer(String addressId, UUID uuid) {
        CaughtPlayersTable log = new CaughtPlayersTable();
        log.bound_address_id = addressId;
        log.playerUniqueId = uuid.toString();
        log.timestamp = System.currentTimeMillis();
        NanoGuardMain.getInstance().getDataStore().save(log);
    }
}