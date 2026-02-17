package de.evoxy.nanoguard.check;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.evoxy.flux.query.Query;
import de.evoxy.nanoguard.NanoGuardApi;
import de.evoxy.nanoguard.config.DefaultConfig;
import de.evoxy.nanoguard.database.BlockedAddressesTable;
import de.evoxy.nanoguard.database.CaughtPlayersTable;
import de.evoxy.nanoguard.player.NanoPlayer;
import de.evoxy.nanoguard.result.VpnResult;

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

                if(!new Query<>(BlockedAddressesTable.class, NanoGuardApi.getInstance().getDataStore()).where("address", ip).execute().isEmpty()){
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
                    String id = UUID.randomUUID().toString();

                    handleDatabaseEntry(id, ip, nanoPlayer.uniqueId(), ipData);
                }

                //TODO: remove second save block.
                return fromJson(ip, responseJson);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return null;
            }
        });
    }

    private static JsonObject fetchIpData(String ip) throws Exception {
        DefaultConfig defaultConfig = NanoGuardApi.getInstance().getDefaultConfig();
        URL url = new URI(API_URL + ip + (Objects.equals(defaultConfig.api_key, "") ? "" : "?key=" + defaultConfig.api_key)).toURL();

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(TIMEOUT);

        try (InputStream is = conn.getInputStream(); Scanner scanner = new Scanner(is)) {
            return scanner.hasNext() ? JsonParser.parseString(scanner.useDelimiter("\\A").next()).getAsJsonObject() : null;
        }
    }

    private static void handleDatabaseEntry(String id, String ip, UUID uuid, JsonObject ipData) {
        Optional<BlockedAddressesTable> existing = new Query<>(BlockedAddressesTable.class, NanoGuardApi.getInstance().getDataStore())
                .where("address", ip)
                .execute().stream().findFirst();

        String addressId = existing.map(table -> table.id).orElseGet(() -> saveNewBlockedAddress(id, ip, ipData).vpnResultId());
        saveCaughtPlayer(addressId, uuid);
    }

    private static VpnResult saveNewBlockedAddress(String id, String ip, JsonObject ipData) {
        BlockedAddressesTable entry = new BlockedAddressesTable();
        entry.id = id;
        entry.address = ip;
        entry.timestamp = System.currentTimeMillis();
        entry.information = fromJson(ipData).toJsonObject().toString();

        NanoGuardApi.getInstance().getDataStore().save(entry);
        return fromJson(id, ipData);
    }

    private static VpnResult fromJson(String id, JsonObject jsonObject){
        JsonObject loc = jsonObject.getAsJsonObject("location");
        JsonObject det = jsonObject.getAsJsonObject("detections");

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
        info.addProperty("operator_name", jsonObject.get("operator").getAsJsonObject().get("name").getAsString());

        flagged = isVpn || isTor || isProxy;

        return new VpnResult(id, continentName, continentCode, countryName, countryCode, isProxy, isVpn, isTor, flagged, risk);
    }

    private static VpnResult fromJson(JsonObject jsonObject){
        JsonObject loc = jsonObject.getAsJsonObject("location");
        JsonObject det = jsonObject.getAsJsonObject("detections");

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
        info.addProperty("operator_name", jsonObject.get("operator").getAsJsonObject().get("name").getAsString());

        flagged = isVpn || isTor || isProxy;

        return new VpnResult(" ", continentName, continentCode, countryName, countryCode, isProxy, isVpn, isTor, flagged, risk);
    }

    private static VpnResult nullResult(){
        return new VpnResult("", "", "", "", "", false, false, false, false, 0);
    }

    private static void saveCaughtPlayer(String addressId, UUID uuid) {
        CaughtPlayersTable log = new CaughtPlayersTable();
        log.bound_address_id = addressId;
        log.playerUniqueId = uuid.toString();
        log.timestamp = System.currentTimeMillis();
        NanoGuardApi.getInstance().getDataStore().save(log);
    }
}
