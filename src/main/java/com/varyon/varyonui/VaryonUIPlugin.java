package com.varyon.varyonui;

import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.varyon.varyonui.config.AdminCommandsConfig;
import com.varyon.varyonui.config.CommandsConfig;
import com.varyon.varyonui.config.NewsConfig;
import com.varyon.varyonui.config.HomeConfig;
import com.varyon.varyonui.config.TutorielConfig;
import com.varyon.varyonui.config.VaryonConfig;

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
        TutorielConfig.getInstance();
        VaryonConfig.getInstance();

        CommandManager mgr = CommandManager.get();
        mgr.register(new UICommand("commands", "Ouvre les commandes", "commandes", "c", "command"));
        mgr.register(new UICommand("tutorial", "Ouvre le tutoriel", "tutoriel", "tuto", "tutoriel"));
        mgr.register(new UICommand("acc", "Ouvre l'accueil", "home", "accueil"));
        mgr.register(new UICommand("ad", "Ouvre le panneau admin", "admin"));
        mgr.register(new UICommand("actus", "Ouvre les actualites", "misesajour", "actualites", "actualités", "actu"));
        mgr.register(new UICommand("profil", "Ouvre le profil", "profil", "profile"));
        mgr.register(new UICommand("param", "Ouvre les paramètres", "parametres", "parametres"));
        mgr.register(new UICommand("var", "Ouvre la page Varyon", "varyon", "varyon"));
        mgr.register(new ReloadCommand());
    }

    public static VaryonUIPlugin getInstance() {
        return instance;
    }

    public File getDataFolder() {
        return dataFolder;
    }
}
