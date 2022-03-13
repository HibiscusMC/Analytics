package me.lorenzo0111.analytics.http;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.lorenzo0111.analytics.Analytics;
import okhttp3.*;
import org.apache.commons.codec.binary.Base64;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameAnalyticsClient {
    private final MediaType json = MediaType.parse("application/json; charset=utf-8");
    private final OkHttpClient httpClient = new OkHttpClient();
    private final String gameKey;
    private final String secretKey;
    private final Map<UUID,Integer> sessionNum = new HashMap<>();
    private final Analytics plugin;

    public GameAnalyticsClient(Analytics plugin, String gameKey, String secretKey) {
        this.plugin = plugin;
        this.gameKey = gameKey;
        this.secretKey = secretKey;
    }

    public void startSession(UUID sessionId) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                int session = getSession(sessionId);

                JsonObject event = prepareRequest(sessionId,session);
                event.addProperty("category", "user");
                simpleCall(sendEvent(event));
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to start session");
                e.printStackTrace();
            }
        });
    }

    public void closeSession(UUID sessionId, long time) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                int session = getSession(sessionId);

                JsonObject event = prepareRequest(sessionId, session);
                event.addProperty("category", "session_end");
                event.addProperty("length", time);
                simpleCall(sendEvent(event));
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to close session");
                e.printStackTrace();
            }

            sessionNum.remove(sessionId);
        });
    }

    @NotNull
    private JsonObject prepareRequest(@NotNull UUID sessionId, int session) {
        JsonObject event = new JsonObject();
        event.addProperty("platform", "ios");
        event.addProperty("os_version", "ios 8.1");
        event.addProperty("device", "iPhone6.1");
        event.addProperty("manufacturer", "apple");
        event.addProperty("sdk_version", "rest api v2");
        event.addProperty("user_id", sessionId.toString());
        event.addProperty("session_id", sessionId.toString());
        event.addProperty("session_num", session);
        event.addProperty("v", 2);
        event.addProperty("client_ts",System.currentTimeMillis() / 1000L);
        return event;
    }

    private static String generateHash(String json, String secretKey) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            byte[] encoded = secretKey.getBytes();
            SecretKeySpec secretKeySpec = new SecretKeySpec(encoded, "HmacSHA256");
            sha256_HMAC.init(secretKeySpec);
            return Base64.encodeBase64String(sha256_HMAC.doFinal(json.getBytes()));
        } catch(Exception ex) {
            return "";
        }
    }

    @NotNull
    private Call sendEvent(@NotNull JsonObject eventJson) {
        JsonArray array = new JsonArray();
        array.add(eventJson);

        plugin.getLogger().info("Calling: " + eventJson);

        return sendEvents(array);
    }

    @NotNull
    private Call sendEvents(@NotNull JsonArray eventJson) {
        return createCall("events", eventJson);
    }

    @NotNull
    private Call createCall(String params, @NotNull JsonElement payload) {
        Request request = new Request.Builder()
                .url("https://api.gameanalytics.com/v2/" + gameKey + "/" + params)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Authorization", generateHash(payload.toString(), secretKey))
                .post(RequestBody.create(payload.toString(), json))
                .build();

        return httpClient.newCall(request);
    }

    private void simpleCall(@NotNull Call call) {
        call.enqueue(new Callback() {

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                plugin.getLogger().info("Response: " + response.body().string());
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                plugin.getLogger().severe("Failed: " + e.getMessage());
            }
        });
    }

    private int getSession(UUID sessionId) throws SQLException {
        if (sessionNum.containsKey(sessionId)) {
            return sessionNum.get(sessionId);
        }

        PreparedStatement statement = plugin.getConnection()
                .prepareStatement("SELECT * FROM players WHERE uuid = ?");

        statement.setString(1, sessionId.toString());
        ResultSet resultSet = statement.executeQuery();
        if(resultSet.next()) {
            sessionNum.put(sessionId, resultSet.getInt("id"));
        } else {
            PreparedStatement insert = plugin.getConnection()
                    .prepareStatement("INSERT INTO players (uuid) VALUES (?)");
            insert.setString(1, sessionId.toString());
            insert.executeUpdate();
        }

        return getSession(sessionId);
    }
}
