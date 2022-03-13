package me.lorenzo0111.analytics.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.lorenzo0111.analytics.Analytics;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PapiHook extends PlaceholderExpansion {
    private final Analytics plugin;

    public PapiHook(Analytics plugin) {
        super();
        this.plugin = plugin;

        this.register();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "analytics";
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().get(0);
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        return super.onRequest(player, params);
    }
}
