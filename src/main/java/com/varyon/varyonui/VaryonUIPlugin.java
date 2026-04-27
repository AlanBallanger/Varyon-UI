package com.varyon.varyonui;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.HudManager;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.varyon.varyonui.hud.VaryonMenuHud;
import com.varyon.varyonui.config.AdminCommandsConfig;
import com.varyon.varyonui.config.CommandsConfig;
import com.varyon.varyonui.config.NewsConfig;
import com.varyon.varyonui.config.HomeConfig;
import com.varyon.varyonui.config.TutorielConfig;
import com.varyon.varyonui.config.VaryonConfig;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VaryonUIPlugin extends JavaPlugin {

    private static final Logger LOG = Logger.getLogger("VaryonUI");

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

        getEventRegistry().registerGlobal(PlayerReadyEvent.class, this::onPlayerReady);
    }

    private void onPlayerReady(PlayerReadyEvent event) {
        Player player = event.getPlayer();
        @SuppressWarnings("rawtypes")
        Ref ref = event.getPlayerRef();
        if (player == null || ref == null) {
            return;
        }
        @SuppressWarnings("rawtypes")
        Store store = ref.getStore();
        if (store == null) {
            return;
        }
        World world = ((EntityStore) store.getExternalData()).getWorld();
        if (world == null) {
            return;
        }

        world.execute(() -> {
            try {
                PlayerRef playerRef = (PlayerRef) store.getComponent(ref, PlayerRef.getComponentType());
                if (playerRef == null) {
                    return;
                }
                HudManager hudManager = player.getHudManager();
                if (hudManager == null) {
                    return;
                }
                hudManager.setCustomHud(playerRef, new VaryonMenuHud(playerRef));
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Failed to register Varyon menu HUD", e);
            }
        });
    }

    public static VaryonUIPlugin getInstance() {
        return instance;
    }

    public File getDataFolder() {
        return dataFolder;
    }
}
