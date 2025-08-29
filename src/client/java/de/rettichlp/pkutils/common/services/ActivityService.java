package de.rettichlp.pkutils.common.services;

import com.google.gson.Gson;
import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import org.jetbrains.annotations.NotNull;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.google.common.hash.Hashing.sha256;
import static de.rettichlp.pkutils.PKUtils.LOGGER;
import static de.rettichlp.pkutils.PKUtilsClient.player;
import static java.net.URI.create;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;

public class ActivityService extends PKUtilsBase {

    private static final String PROXY_URL = "https://activitycheck.pkutils.eu/proxy";
    private static final String CLEAR_URL = "https://activitycheck.pkutils.eu/clearactivity";
    private static final String SERVER_SECRET_SALT = "^HnBssKA:?qj8@..d!t!BA^vu9vq5y";

    private final Gson gson = new Gson();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String sessionToken = MinecraftClient.getInstance().getSession().getAccessToken();

    public void trackActivity(String activityType, String successMessage) {
        ServerInfo serverInfo = MinecraftClient.getInstance().getCurrentServerEntry();
        if (serverInfo == null) {
            sendModMessage("Du bist auf keinem Server.", false);
            return;
        }

        String serverIp = serverInfo.address;
        String playerName = player.getName().getString();

        String serverHash = createSha256(serverIp + this.sessionToken + SERVER_SECRET_SALT);

        ActivityRequest activityRequest = ActivityRequest.builder()
                .playerName(playerName)
                .activity(activityType)
                .sessionToken(this.sessionToken)
                .serverHash(serverHash)
                .build();

        HttpRequest request = getHttpRequest(activityRequest.toJsonString(), PROXY_URL);

        new Thread(() -> {
            try {
                HttpResponse<String> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                ActivityResponse activityResponse = this.gson.fromJson(response.body(), ActivityResponse.class);
                LOGGER.info("Tracked activity: {}", activityResponse.getMessage());
                sendModMessage(response.statusCode() == 200 ? successMessage : "Fehler beim Tracken der Aktivität!", false);
            } catch (Exception e) {
                LOGGER.error("Failed to send activity to proxy", e);
                sendModMessage("PKUtils-Server nicht erreichbar!", true);
            }
        }).start();
    }

    public void clearActivity(String targetName) {
        ActivityClearRequest activityClearRequest = ActivityClearRequest.builder()
                .playerName(targetName)
                .sessionToken(this.sessionToken)
                .build();

        HttpRequest request = getHttpRequest(activityClearRequest.toJsonString(), CLEAR_URL);

        new Thread(() -> {
            try {
                HttpResponse<String> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                ActivityResponse activityResponse = this.gson.fromJson(response.body(), ActivityResponse.class);
                LOGGER.info("Cleared activity response: {}", activityResponse.getMessage());
                sendModMessage(activityResponse.getMessage(), false);
            } catch (Exception e) {
                LOGGER.error("Failed to send clear activity to proxy", e);
                sendModMessage("PKUtils-Server nicht erreichbar!", true);
            }
        }).start();
    }

    private @NotNull String createSha256(CharSequence input) {
        return sha256()
                .hashString(input, UTF_8)
                .toString();
    }

    private HttpRequest getHttpRequest(String body, String url) {
        return HttpRequest.newBuilder()
                .uri(create(url))
                .header(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
    }

    @Builder
    @AllArgsConstructor
    private static class ActivityRequest {

        private final String playerName;
        private final String activity;
        private final String sessionToken;
        private final String serverHash;

        public String toJsonString() {
            return new Gson().toJson(this);
        }
    }

    @Builder
    @AllArgsConstructor
    private static class ActivityClearRequest {

        private final String playerName;
        private final String sessionToken;

        public String toJsonString() {
            return new Gson().toJson(this);
        }
    }

    @Getter
    @AllArgsConstructor
    private static class ActivityResponse {

        private final String message;
    }
}
