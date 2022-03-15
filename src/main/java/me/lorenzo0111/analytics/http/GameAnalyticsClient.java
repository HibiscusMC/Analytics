package me.lorenzo0111.analytics.http;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.lorenzo0111.analytics.Analytics;
import okhttp3.*;
import org.apache.commons.codec.binary.Base64;
import org.jetbrains.annotations.NotNull;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.util.*;

public class GameAnalyticsClient {
    private final MediaType json = MediaType.parse("application/json; charset=utf-8");
    private final OkHttpClient httpClient = new OkHttpClient();
    private final String gameKey;
    private final String secretKey;
    private final Map<UUID,UUID> sessions = new HashMap<>();
    private final Map<UUID,Integer> sessionNum = new HashMap<>();
    private final Analytics plugin;

    public GameAnalyticsClient(Analytics plugin, String gameKey, String secretKey) {
        this.plugin = plugin;
        this.gameKey = gameKey;
        this.secretKey = secretKey;
    }

    public void startSession(UUID player) {
        UUID sessionId = sessions.computeIfAbsent(player, k -> UUID.randomUUID());
        int session = getSession(sessionId);

        JsonObject event = prepareRequest(sessionId, player, session);
        event.addProperty("category", "user");

        simpleCall(sendEvent(event));
    }

    public void closeSession(UUID player, long time) {
        final UUID sessionId = sessions.computeIfAbsent(player, k -> UUID.randomUUID());
        int session = getSession(sessionId);

        JsonObject event = prepareRequest(sessionId, player, session);
        event.addProperty("category", "session_end");
        event.addProperty("length", time);
        simpleCall(sendEvent(event));

        sessions.remove(player, sessionId);
        sessionNum.remove(sessionId);
    }

    public void processShop(UUID player, String item, int price, String currency, int id) {
        final UUID sessionId = sessions.computeIfAbsent(player, k -> UUID.randomUUID());
        int session = getSession(sessionId);

        JsonObject event = prepareRequest(sessionId, player, session);
        event.addProperty("category", "business");
        event.addProperty("event_id", "item:" + item);
        event.addProperty("amount", price);
        event.addProperty("currency", currency);
        event.addProperty("transaction_num", id);

        simpleCall(sendEvent(event));
    }

    @NotNull
    private JsonObject prepareRequest(@NotNull UUID sessionId, UUID userId, int session) {
        JsonObject event = new JsonObject();
        event.addProperty("platform", "ios");
        event.addProperty("os_version", "ios 8.1");
        event.addProperty("device", "iPhone6.1");
        event.addProperty("manufacturer", "apple");
        event.addProperty("sdk_version", "rest api v2");
        event.addProperty("user_id", userId.toString());
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

    private int getSession(UUID sessionId) {
        if (!sessionNum.containsKey(sessionId)) {
            sessionNum.put(sessionId, plugin.getSessionNum().incrementAndGet());
        }

        return sessionNum.get(sessionId);
    }
}
