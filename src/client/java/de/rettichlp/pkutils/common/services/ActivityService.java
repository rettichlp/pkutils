package de.rettichlp.pkutils.common.services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.rettichlp.pkutils.common.manager.PKUtilsBase;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CompletableFuture;

import static de.rettichlp.pkutils.PKUtils.LOGGER;
import static de.rettichlp.pkutils.PKUtilsClient.player;

public class ActivityService extends PKUtilsBase {

    private static final String PROXY_URL = "https://activitycheck.pkutils.eu/proxy";
    private static final String CLEAR_URL = "https://activitycheck.pkutils.eu/clearactivity";

    private static final String SERVER_SECRET_SALT = "^HnBssKA:?qj8@..d!t!BA^vu9vq5y"; // ÄNDERN!

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public void trackActivity(String activityType, String successMessage) {
        if (player == null) return;

        ServerInfo serverInfo = MinecraftClient.getInstance().getCurrentServerEntry();
        if (serverInfo == null) {
            sendModMessage("§cFehler: Du bist nicht auf einem Server.", false);
            return;
        }

        String serverIp = serverInfo.address;
        String playerName = player.getName().getString();
        String sessionToken = MinecraftClient.getInstance().getSession().getAccessToken();

        String serverHash = createSha256(serverIp + sessionToken + SERVER_SECRET_SALT);
        if (serverHash == null) {
            sendModMessage("§cFehler bei der Hash-Erstellung.", false);
            return;
        }

        JsonObject json = new JsonObject();
        json.addProperty("playerName", playerName);
        json.addProperty("activity", activityType);
        json.addProperty("sessionToken", sessionToken);
        json.addProperty("serverHash", serverHash);

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

    public void clearActivity(String targetName) {
        if (player == null) return;

        String sessionToken = MinecraftClient.getInstance().getSession().getAccessToken();

        JsonObject json = new JsonObject();
        json.addProperty("sessionToken", sessionToken);
        json.addProperty("targetName", targetName);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(CLEAR_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                .build();

        sendModMessage("Sende Anfrage zum Zurücksetzen...", false);

        CompletableFuture.runAsync(() -> {
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                JsonObject responseJson = JsonParser.parseString(response.body()).getAsJsonObject();

                if (response.statusCode() == 200) {
                    sendModMessage("§a" + responseJson.get("message").getAsString(), false);
                } else {
                    sendModMessage("§cFehler: " + responseJson.get("error").getAsString(), false);
                }
            } catch (Exception e) {
                LOGGER.error("Fehler beim Senden der Clear-Anfrage.", e);
                sendModMessage("§cServer nicht erreichbar oder fehlerhafte Antwort.", false);
            }
        });
    }

    private String createSha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("SHA-256 Algorithmus nicht gefunden.", e);
            return null;
        }
    }
}