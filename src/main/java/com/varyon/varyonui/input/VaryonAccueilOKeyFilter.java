package com.varyon.varyonui.input;

import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.packets.interaction.SyncInteractionChain;
import com.hypixel.hytale.protocol.packets.interaction.SyncInteractionChains;
import com.hypixel.hytale.server.core.io.adapter.PlayerPacketFilter;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nonnull;

public final class VaryonAccueilOKeyFilter implements PlayerPacketFilter {

    @Override
    public boolean test(@Nonnull PlayerRef playerRef, @Nonnull Packet packet) {
        if (!(packet instanceof SyncInteractionChains sync)) {
            return false;
        }
        if (sync.updates == null || sync.updates.length == 0) {
            return false;
        }
        for (SyncInteractionChain chain : sync.updates) {
            if (chain == null) {
                continue;
            }
            if (chain.interactionType == InteractionType.GameModeSwap) {
                VaryonOKeyPending.mark(playerRef);
                return false;
            }
        }
        return false;
    }
}
