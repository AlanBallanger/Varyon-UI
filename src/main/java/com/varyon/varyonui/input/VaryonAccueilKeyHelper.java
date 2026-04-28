package com.varyon.varyonui.input;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class VaryonAccueilKeyHelper {

    private static final Logger LOG = Logger.getLogger("VaryonUI");
    private static final String ACCUEIL_COMMAND = "accueil";

    private VaryonAccueilKeyHelper() {
    }

    public static void runAccueil(@Nonnull PlayerRef playerRef) {
        if (playerRef == null || !playerRef.isValid()) {
            return;
        }
        try {
            Ref<?> ref = playerRef.getReference();
            if (ref == null || !ref.isValid()) {
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
                    CommandManager.get().handleCommand(playerRef, ACCUEIL_COMMAND);
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "VaryonUI: /accueil from key", e);
                }
            });
        } catch (Exception e) {
            LOG.log(Level.WARNING, "VaryonUI: schedule /accueil", e);
        }
    }
}
