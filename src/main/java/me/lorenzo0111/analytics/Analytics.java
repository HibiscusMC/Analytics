package me.lorenzo0111.analytics;

import me.lorenzo0111.analytics.http.GameAnalyticsClient;
import me.lorenzo0111.analytics.listeners.PlayerListener;
import me.lorenzo0111.analytics.papi.PapiHook;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.atomic.AtomicInteger;

public final class Analytics extends JavaPlugin {
    private GameAnalyticsClient client;
    private AtomicInteger sessionNum;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        this.sessionNum = new AtomicInteger(this.getConfig().getInt("sessionNum"));
        this.client = new GameAnalyticsClient(this, this.getConfig().getString("gameKey"), this.getConfig().getString("secretKey"));

        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);

        new PapiHook(this);

        this.getLogger().info("Analytics started successfully!");
    }

    @Override
    public void onDisable() {
        this.getConfig().set("sessionNum", this.sessionNum.get());
    }

    public GameAnalyticsClient getClient() {
        return client;
    }

    public AtomicInteger getSessionNum() {
        return sessionNum;
    }
}
