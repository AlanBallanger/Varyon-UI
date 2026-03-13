package com.varyon.varyonui;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.varyon.varyonui.hud.AccueilHUDManager;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class AccueilHUDToggleCommand extends AbstractAsyncCommand {

    private final AccueilHUDManager hudManager;

    public AccueilHUDToggleCommand(@Nonnull AccueilHUDManager hudManager) {
        super("accueilhud", "Active ou désactive l'affichage du hint /accueil en bas de l'écran");
        this.hudManager = hudManager;
    }

    @Override
    @Nonnull
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {
        if (!ctx.isPlayer()) {
            ctx.sendMessage(Message.raw("Cette commande doit être exécutée par un joueur"));
            return CompletableFuture.completedFuture(null);
        }

        Ref<EntityStore> playerRef = ctx.senderAsPlayerRef();
        if (playerRef == null || !playerRef.isValid()) {
            return CompletableFuture.completedFuture(null);
        }

        Store<EntityStore> store = playerRef.getStore();
        World world = store.getExternalData().getWorld();

        return CompletableFuture.runAsync(() -> {
            PlayerRef playerRefComponent = store.getComponent(playerRef, PlayerRef.getComponentType());
            Player playerComponent = store.getComponent(playerRef, Player.getComponentType());
            if (playerRefComponent == null || playerComponent == null) return;

            boolean current = hudManager.isAccueilHintEnabled(playerRefComponent.getUuid());
            hudManager.setAccueilHintEnabled(playerRefComponent.getUuid(), !current);
            hudManager.refreshPlayer(playerComponent, playerRefComponent);

            if (current) {
                ctx.sendMessage(Message.raw("Hint /accueil désactivé. Tape /accueilhud ou /param pour le réactiver."));
            } else {
                ctx.sendMessage(Message.raw("Hint /accueil activé."));
            }
        }, world);
    }
}
