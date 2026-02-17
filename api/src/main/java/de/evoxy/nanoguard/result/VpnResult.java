package de.evoxy.nanoguard.result;

import com.google.gson.JsonObject;

public record VpnResult(String vpnResultId, String continentName, String continentCode, String countryName, String countryCode, boolean isProxy, boolean isVpn, boolean isTor, boolean flagged, int risk) {

    public JsonObject toJsonObject(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("resultId", vpnResultId);
        jsonObject.addProperty("continent_name", continentName);
        jsonObject.addProperty("continent_code", continentCode);
        jsonObject.addProperty("country_name", countryName);
        jsonObject.addProperty("country_code", countryCode);
        jsonObject.addProperty("is_vpn", isVpn);
        jsonObject.addProperty("is_proxy", isProxy);
        jsonObject.addProperty("is_tor", isTor);
        jsonObject.addProperty("risk", risk);
        return jsonObject;
    }

}
