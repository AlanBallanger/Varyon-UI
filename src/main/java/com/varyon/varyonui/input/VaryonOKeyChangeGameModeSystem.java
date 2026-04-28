package com.varyon.varyonui.input;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.event.events.ecs.ChangeGameModeEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public final class VaryonOKeyChangeGameModeSystem extends EntityEventSystem<EntityStore, ChangeGameModeEvent> {

    private static final ComponentType<EntityStore, PlayerRef> PLAYER_REF = PlayerRef.getComponentType();

    public VaryonOKeyChangeGameModeSystem() {
        super(ChangeGameModeEvent.class);
    }

    @Override
    public void handle(
        int index,
        @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
        @Nonnull Store<EntityStore> store,
        @Nonnull CommandBuffer<EntityStore> commandBuffer,
        @Nonnull ChangeGameModeEvent event) {
        GameMode target = event.getGameMode();
        if (target != GameMode.Creative && target != GameMode.Adventure) {
            return;
        }
        PlayerRef playerRef = archetypeChunk.getComponent(index, PLAYER_REF);
        if (playerRef == null) {
            return;
        }
        UUID uuid = playerRef.getUuid();
        if (uuid == null) {
            return;
        }
        if (!VaryonOKeyPending.tryConsumeForPlayer(uuid)) {
            return;
        }
        event.setCancelled(true);
        VaryonAccueilKeyHelper.runAccueil(playerRef);
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return PLAYER_REF;
    }
}
