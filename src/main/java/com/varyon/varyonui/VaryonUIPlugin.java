package com.varyon.varyonui;

import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.varyon.varyonui.config.AdminCommandsConfig;
import com.varyon.varyonui.config.CommandsConfig;
import com.varyon.varyonui.config.NewsConfig;
import com.varyon.varyonui.config.HomeConfig;

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
        AdminCommandsConfig.getInstance();
        NewsConfig.getInstance();
        HomeConfig.getInstance();

        CommandManager mgr = CommandManager.get();
        mgr.register(new UICommand("commands", "Ouvre les commandes", "commandes", "c", "command"));
        mgr.register(new UICommand("tutorial", "Ouvre le tutoriel", "tutoriel", "tuto", "tutoriel", "t"));
        mgr.register(new UICommand("home", "Ouvre l'accueil", "home", "accueil", "a", "h"));
        mgr.register(new UICommand("actus", "Ouvre les actualites", "misesajour", "actualites", "actualités", "actu"));
        mgr.register(new UICommand("info", "Ouvre les infos", "infos", "i"));
        mgr.register(new UICommand("varyon", "Ouvre la page Varyon", "varyon", "v"));
        mgr.register(new ReloadCommand());
    }

    public static VaryonUIPlugin getInstance() {
        return instance;
    }

    public File getDataFolder() {
        return dataFolder;
    }
}
