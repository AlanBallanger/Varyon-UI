package com.varyon.varyonui.hud;

import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nonnull;

public class AccueilHUD extends CustomUIHud {

    private final String message;

    public AccueilHUD(@Nonnull PlayerRef playerRef, @Nonnull String message) {
        super(playerRef);
        this.message = message;
    }

    @Override
    protected void build(@Nonnull UICommandBuilder builder) {
        builder.append("HUD/AccueilHUD.ui");
        builder.set("#AccueilHint.Text", message);
    }
}
