package me.lorenzo0111.analytics.listeners;

import me.lorenzo0111.analytics.Analytics;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerListener implements Listener {
    private final Analytics plugin;
    private final Map<UUID, Instant> playTimes = new HashMap<>();

    public PlayerListener(Analytics plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent event) {
        plugin.getClient().startSession(event.getPlayer().getUniqueId());
        playTimes.put(event.getPlayer().getUniqueId(), Instant.now());
    }

    @EventHandler
    public void onQuit(@NotNull PlayerQuitEvent event) {
        long playTime = Duration.between(playTimes.get(event.getPlayer().getUniqueId()), Instant.now()).toSeconds();

        plugin.getClient().closeSession(event.getPlayer().getUniqueId(), playTime);
        playTimes.remove(event.getPlayer().getUniqueId());
    }
}
