package com.varyon.varyonui.hud;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.varyon.varyonui.config.AccueilHUDConfig;
import com.varyon.varyonui.config.PlayerSettingsStore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AccueilHUDManager {
    private static final Logger LOGGER = Logger.getLogger(AccueilHUDManager.class.getName());

    private final AccueilHUDConfig config;
    private final PlayerSettingsStore settingsStore;
    private final Map<UUID, AccueilHUD> playerHuds = new ConcurrentHashMap<>();
    private final Map<UUID, Player> playerCache = new ConcurrentHashMap<>();

    public AccueilHUDManager(@Nonnull AccueilHUDConfig config, @Nonnull PlayerSettingsStore settingsStore) {
        this.config = config;
        this.settingsStore = settingsStore;
    }

    public void registerPlayer(@Nonnull Player player, @Nonnull PlayerRef playerRef) {
        if (!config.isEnabled()) return;
        if (!settingsStore.isAccueilHintEnabled(playerRef.getUuid())) return;

        AccueilHUD hud = new AccueilHUD(playerRef, config.getMessage());
        playerHuds.put(playerRef.getUuid(), hud);
        playerCache.put(playerRef.getUuid(), player);
        try {
            player.getHudManager().setCustomHud(playerRef, hud);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to register AccueilHUD: " + e.getMessage());
        }
    }

    public void removePlayer(@Nonnull UUID playerId) {
        playerHuds.remove(playerId);
        Player player = playerCache.remove(playerId);
        removeHud(player, playerId);
    }

    private void removeHud(@Nullable Player player, @Nonnull UUID playerId) {
        if (player == null) return;
        try {
            PlayerRef playerRef = Universe.get().getPlayer(playerId);
            if (playerRef != null && playerRef.isValid()) {
                player.getHudManager().setCustomHud(playerRef, null);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to remove AccueilHUD: " + e.getMessage());
        }
    }

    public void refreshPlayer(@Nonnull Player player, @Nonnull PlayerRef playerRef) {
        removePlayer(playerRef.getUuid());
        registerPlayer(player, playerRef);
    }

    public boolean isAccueilHintEnabled(@Nonnull UUID playerId) {
        return settingsStore.isAccueilHintEnabled(playerId);
    }

    public void setAccueilHintEnabled(@Nonnull UUID playerId, boolean enabled) {
        settingsStore.setAccueilHintEnabled(playerId, enabled);
    }
}
