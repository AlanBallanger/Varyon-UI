package com.varyon.varyonui.config;

import com.varyon.varyonui.VaryonUIPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AccueilHUDConfig {
    private static final Logger LOGGER = Logger.getLogger(AccueilHUDConfig.class.getName());
    private static final String CONFIG_FILE = "accueil_hud.properties";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_ENABLED = "enabled";
    private static final String DEFAULT_MESSAGE = "/accueil pour découvrir ce que propose le serveur (désactivable)";
    private static final boolean DEFAULT_ENABLED = true;

    private String message = DEFAULT_MESSAGE;
    private boolean enabled = DEFAULT_ENABLED;
    private final Path configPath;

    public AccueilHUDConfig() {
        this.configPath = VaryonUIPlugin.getInstance().getDataFolder().toPath().resolve(CONFIG_FILE);
        load();
    }

    private void load() {
        if (!Files.exists(configPath)) {
            save();
            return;
        }
        try (var reader = Files.newBufferedReader(configPath)) {
            var props = new Properties();
            props.load(reader);
            message = props.getProperty(KEY_MESSAGE, DEFAULT_MESSAGE);
            enabled = Boolean.parseBoolean(props.getProperty(KEY_ENABLED, String.valueOf(DEFAULT_ENABLED)));
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to load AccueilHUD config: " + e.getMessage());
        }
    }

    private void save() {
        try {
            Files.createDirectories(configPath.getParent());
            var props = new Properties();
            props.setProperty(KEY_MESSAGE, message);
            props.setProperty(KEY_ENABLED, String.valueOf(enabled));
            try (var writer = Files.newBufferedWriter(configPath)) {
                props.store(writer, "AccueilHUD Configuration");
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to save AccueilHUD config: " + e.getMessage());
        }
    }

    public String getMessage() {
        return message;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setMessage(String message) {
        this.message = message;
        save();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        save();
    }

    public void reload() {
        load();
    }
}
