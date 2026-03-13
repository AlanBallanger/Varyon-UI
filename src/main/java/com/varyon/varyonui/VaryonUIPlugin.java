package com.varyon.varyonui;

import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.varyon.varyonui.config.AccueilHUDConfig;
import com.varyon.varyonui.config.PlayerSettingsStore;
import com.varyon.varyonui.config.AdminCommandsConfig;
import com.varyon.varyonui.config.CommandsConfig;
import com.varyon.varyonui.config.NewsConfig;
import com.varyon.varyonui.config.HomeConfig;
import com.varyon.varyonui.config.TutorielConfig;
import com.varyon.varyonui.config.VaryonConfig;
import com.varyon.varyonui.hud.AccueilHUDManager;

import java.io.File;

public class VaryonUIPlugin extends JavaPlugin {
    private static VaryonUIPlugin instance;
    private File dataFolder;
    private AccueilHUDManager accueilHUDManager;

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

        AccueilHUDConfig accueilHUDConfig = new AccueilHUDConfig();
        PlayerSettingsStore playerSettingsStore = new PlayerSettingsStore();
        accueilHUDManager = new AccueilHUDManager(accueilHUDConfig, playerSettingsStore);

        this.getEventRegistry().registerGlobal(com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent.class, event -> {
            Player player = event.getPlayer();
            Ref ref = event.getPlayerRef();
            Store store = ref.getStore();
            World world = ((EntityStore) store.getExternalData()).getWorld();
            world.execute(() -> {
                PlayerRef playerRef = (PlayerRef) store.getComponent(ref, PlayerRef.getComponentType());
                if (playerRef != null) {
                    accueilHUDManager.registerPlayer(player, playerRef);
                }
            });
        });

        this.getEventRegistry().registerGlobal(com.hypixel.hytale.server.core.event.events.player.DrainPlayerFromWorldEvent.class, event -> {
            PlayerRef playerRef = event.getHolder().getComponent(PlayerRef.getComponentType());
            if (playerRef != null) {
                accueilHUDManager.removePlayer(playerRef.getUuid());
            }
        });

        this.getEventRegistry().registerGlobal(com.hypixel.hytale.server.core.event.events.player.AddPlayerToWorldEvent.class, event -> {
            PlayerRef playerRef = event.getHolder().getComponent(PlayerRef.getComponentType());
            Player player = event.getHolder().getComponent(Player.getComponentType());
            if (playerRef != null && player != null) {
                accueilHUDManager.registerPlayer(player, playerRef);
            }
        });

        this.getEventRegistry().registerGlobal(com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent.class, event -> {
            accueilHUDManager.removePlayer(event.getPlayerRef().getUuid());
        });

        this.getCommandRegistry().registerCommand(new AccueilHUDToggleCommand(accueilHUDManager));

        CommandManager mgr = CommandManager.get();
        mgr.register(new UICommand("commands", "Ouvre les commandes", "commandes", "c", "command"));
        mgr.register(new UICommand("tutorial", "Ouvre le tutoriel", "tutoriel", "tuto", "tutoriel"));
        mgr.register(new UICommand("acc", "Ouvre l'accueil", "home", "accueil"));
        mgr.register(new UICommand("ad", "Ouvre le panneau admin", "admin"));
        mgr.register(new UICommand("actus", "Ouvre les actualites", "misesajour", "actualites", "actualités", "actu"));
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

    public AccueilHUDManager getAccueilHUDManager() {
        return accueilHUDManager;
    }
}
