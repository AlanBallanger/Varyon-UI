package com.varyon.varyonui;

import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.varyon.varyonui.config.CommandsConfig;

import java.io.File;

public class VaryonUIPlugin extends JavaPlugin {
    private static VaryonUIPlugin instance;
    private File dataFolder;

    public VaryonUIPlugin(JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        instance = this;
        this.dataFolder = this.getDataDirectory().toFile();
        
        CommandsConfig.getInstance();
        
        CommandManager.get().register(new UICommand());
        CommandManager.get().register(new ReloadCommand());
    }

    public static VaryonUIPlugin getInstance() {
        return instance;
    }

    public File getDataFolder() {
        return dataFolder;
    }
}
