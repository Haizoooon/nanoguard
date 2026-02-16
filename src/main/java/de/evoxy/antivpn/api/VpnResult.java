package de.evoxy.antivpn.api;

public record VpnResult(String vpnResultId, String continentName, String continentCode, String countryName, String countryCode, boolean isProxy, boolean isVpn, boolean isTor, boolean flagged, int risk) {
}
