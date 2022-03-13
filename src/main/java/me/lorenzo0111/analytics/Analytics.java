package me.lorenzo0111.analytics;

import me.lorenzo0111.analytics.http.GameAnalyticsClient;
import me.lorenzo0111.analytics.listeners.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class Analytics extends JavaPlugin {
    private GameAnalyticsClient client;
    private Connection connection;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        File file = new File(this.getDataFolder(), "database.db");
        if (!file.exists()) {
            this.getLogger().info("Creating database...");
            try {
                file.createNewFile();
            } catch (Exception e) {
                this.getLogger().severe("Could not create database!");
                e.printStackTrace();
                this.setEnabled(false);
                return;
            }
        }

        try {
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
            Statement statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS players (uuid TEXT, id INTEGER PRIMARY KEY AUTOINCREMENT);");
        } catch (SQLException e) {
            this.getLogger().severe("Could not connect to the database!");
            e.printStackTrace();
            this.setEnabled(false);
            return;
        }

        this.client = new GameAnalyticsClient(this, this.getConfig().getString("gameKey"), this.getConfig().getString("secretKey"));

        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
    }

    @Override
    public void onDisable() {
        try {
            if (connection != null) connection.close();
        } catch (SQLException ignored) {}
    }

    public GameAnalyticsClient getClient() {
        return client;
    }

    public Connection getConnection() {
        return connection;
    }
}
