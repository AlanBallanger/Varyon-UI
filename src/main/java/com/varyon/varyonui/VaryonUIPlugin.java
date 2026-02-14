package com.varyon.varyonui;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

public class VaryonUIPlugin extends JavaPlugin {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public VaryonUIPlugin(JavaPluginInit init) {
        super(init);
        LOGGER.atInfo().log("Initialisation de %s version %s", this.getName(), this.getManifest().getVersion().toString());
    }

    @Override
    protected void setup() {
        this.getCommandRegistry().registerCommand(new UICommand());
        LOGGER.atInfo().log("Varyon UI chargé - commande /c disponible !");
    }
}
