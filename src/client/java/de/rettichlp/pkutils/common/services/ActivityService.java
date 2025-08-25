package de.rettichlp.pkutils.common.services;

import com.google.gson.JsonObject;
import de.rettichlp.pkutils.common.manager.PKUtilsBase;
import net.minecraft.client.MinecraftClient;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

import static de.rettichlp.pkutils.PKUtils.LOGGER;
import static de.rettichlp.pkutils.PKUtilsClient.player;

public class ActivityService extends PKUtilsBase {

    private static final String PROXY_URL = "https://activitycheck.pkutils.eu/proxy";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    public void trackActivity(String activityType, String successMessage) {
        if (player == null) return;

        String playerName = player.getName().getString();
        String sessionToken = MinecraftClient.getInstance().getSession().getAccessToken();

        JsonObject json = new JsonObject();
        json.addProperty("playerName", playerName);
        json.addProperty("activity", activityType);
        json.addProperty("sessionToken", sessionToken);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(PROXY_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                .build();

        CompletableFuture.runAsync(() -> {
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    LOGGER.info("Aktivität '{}' für {} erfolgreich getrackt.", activityType, playerName);
                    sendModMessage(successMessage, true);
                } else {
                    LOGGER.warn("Fehler beim Tracken der Aktivität '{}' für {}. Status: {}, Antwort: {}",
                            activityType, playerName, response.statusCode(), response.body());
                    sendModMessage("§cFehler beim Tracken der Aktivität!", true);
                }
            } catch (Exception e) {
                LOGGER.error("Schwerer Fehler beim Senden der Aktivität an den Proxy.", e);
                sendModMessage("§cProxy-Server nicht erreichbar!", true);
            }
        });
    }
}