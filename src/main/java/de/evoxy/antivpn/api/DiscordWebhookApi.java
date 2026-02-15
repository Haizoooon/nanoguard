package de.evoxy.antivpn.api;

import de.evoxy.antivpn.NanoGuardMain;
import de.evoxy.antivpn.config.DefaultConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class DiscordWebhookApi {

    private static final HttpClient httpClient = HttpClient.newBuilder().build();

    public static CompletableFuture<Void> sendWebhookAsync(String content) {

        //DefaultConfig defaultConfig = NanoGuardMain.getInstance().getDefaultConfig();
        String webhookUrl = "https://discord.com/api/webhooks/1471863404665770194/h8Z3b_cZRy5YueKq49bvrZiyz6YmnuYox6JvJiWdLJfBEdXGdhmIkUExB0X5ScUYAPG4"; //defaultConfig.discord.webhook_url;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(webhookUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(content))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenAccept(response -> {
                System.out.println("Send " + (response.statusCode() == 200 ? "successfully" : "not successfully!"));
            })
            .exceptionally(ex -> {
                throw new RuntimeException(ex);
            });
    }

}