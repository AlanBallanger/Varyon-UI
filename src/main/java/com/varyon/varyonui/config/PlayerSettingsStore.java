package com.varyon.varyonui.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.varyon.varyonui.VaryonUIPlugin;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlayerSettingsStore {
    private static final Logger LOGGER = Logger.getLogger(PlayerSettingsStore.class.getName());
    private static final String SETTINGS_FILE = "player_settings.json";
    private static final String KEY_ACCUEIL_HINT = "accueilHintEnabled";
    private static final boolean DEFAULT_ACCUEIL_HINT = true;

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Type mapType = new TypeToken<Map<String, Map<String, Object>>>() {}.getType();
    private final Path settingsPath;
    private Map<String, Map<String, Object>> data = new HashMap<>();

    public PlayerSettingsStore() {
        this.settingsPath = VaryonUIPlugin.getInstance().getDataFolder().toPath().resolve(SETTINGS_FILE);
        load();
        migrateFromLegacy();
    }

    private void migrateFromLegacy() {
        Path legacyPath = settingsPath.getParent().resolve("accueil_hud_disabled.txt");
        if (!Files.exists(legacyPath)) return;
        try {
            Files.lines(legacyPath).filter(s -> !s.isBlank()).map(String::trim).forEach(line -> {
                try {
                    setAccueilHintEnabled(UUID.fromString(line), false);
                } catch (IllegalArgumentException ignored) {
                }
            });
            Files.delete(legacyPath);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to migrate from legacy preferences: " + e.getMessage());
        }
    }

    private void load() {
        if (!Files.exists(settingsPath)) return;
        try {
            String json = Files.readString(settingsPath);
            Map<String, Map<String, Object>> loaded = gson.fromJson(json, mapType);
            if (loaded != null) {
                data = loaded;
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to load player settings: " + e.getMessage());
        }
    }

    private void save() {
        try {
            Files.createDirectories(settingsPath.getParent());
            Files.writeString(settingsPath, gson.toJson(data));
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to save player settings: " + e.getMessage());
        }
    }

    private Map<String, Object> getPlayerSettings(@Nonnull UUID playerId) {
        return data.computeIfAbsent(playerId.toString(), k -> new HashMap<>());
    }

    public boolean isAccueilHintEnabled(@Nonnull UUID playerId) {
        Object val = getPlayerSettings(playerId).get(KEY_ACCUEIL_HINT);
        if (val == null) return DEFAULT_ACCUEIL_HINT;
        if (val instanceof Boolean b) return b;
        return DEFAULT_ACCUEIL_HINT;
    }

    public void setAccueilHintEnabled(@Nonnull UUID playerId, boolean enabled) {
        getPlayerSettings(playerId).put(KEY_ACCUEIL_HINT, enabled);
        save();
    }
}
