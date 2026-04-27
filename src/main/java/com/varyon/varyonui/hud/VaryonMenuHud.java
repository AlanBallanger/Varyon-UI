package com.varyon.varyonui.hud;

import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.Anchor;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nonnull;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VaryonMenuHud extends CustomUIHud {

    private static final Logger LOGGER = Logger.getLogger("VaryonMenuHud");

    private static final int PNG_W = 189;
    private static final int PNG_H = 322;
    private static final int ANCHOR_RIGHT = 12;
    private static final int ANCHOR_BOTTOM = 20;
    private static final int MENU_LIFT_PX = 70;
    private static final int TITLE_SHIFT_RIGHT_PX = 7;
    private static final int TITLE_NUDGE_DOWN_PX = 15;
    private static final int HUD_SHIFT_DOWN_PX = 50;
    private static final int ANCHOR_WIDTH = 128;

    private static final int IMG_W = 118;
    private static final int IMG_H = (int) Math.round((double) IMG_W * (double) PNG_H / (double) PNG_W);
    private static final int KEY_H = 32;
    private static final int KEY_SRC_X = 109;
    private static final int KEY_SRC_Y = 224;
    private static final int KEY_LABEL_W = 32;
    private static final int KEY_NUDGE_DOWN_PX = 1;

    private static final int ANCHOR_HEIGHT = 24 + 28 + IMG_H + MENU_LIFT_PX;

    public VaryonMenuHud(@Nonnull PlayerRef playerRef) {
        super(playerRef);
    }

    @Override
    protected void build(@Nonnull UICommandBuilder builder) {
        try {
            builder.append("HUD/VaryonMenuHud.ui");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to build Varyon menu HUD", e);
            return;
        }
        int menuOffsetY = -MENU_LIFT_PX + HUD_SHIFT_DOWN_PX;

        Anchor root = new Anchor();
        root.setRight(Value.of(ANCHOR_RIGHT));
        root.setBottom(Value.of(ANCHOR_BOTTOM));
        root.setWidth(Value.of(ANCHOR_WIDTH));
        root.setHeight(Value.of(ANCHOR_HEIGHT));
        builder.setObject("#VaryonMenuHud.Anchor", root);
        builder.set("#VaryonMenuTitle.Text", "Menu Varyon");
        builder.set("#VaryonMenuKey.Text", "O");

        Anchor title = new Anchor();
        title.setLeft(Value.of(TITLE_SHIFT_RIGHT_PX));
        title.setTop(Value.of(menuOffsetY + TITLE_NUDGE_DOWN_PX));
        title.setWidth(Value.of(ANCHOR_WIDTH - TITLE_SHIFT_RIGHT_PX));
        title.setHeight(Value.of(24));
        builder.setObject("#VaryonMenuTitle.Anchor", title);

        int stackLeft = (ANCHOR_WIDTH - IMG_W) / 2;
        int stackTop = 28 + menuOffsetY;

        Anchor stack = new Anchor();
        stack.setLeft(Value.of(stackLeft));
        stack.setTop(Value.of(stackTop));
        stack.setWidth(Value.of(IMG_W));
        stack.setHeight(Value.of(IMG_H));
        builder.setObject("#VaryonMenuStack.Anchor", stack);

        Anchor base = new Anchor();
        base.setLeft(Value.of(0));
        base.setTop(Value.of(0));
        base.setWidth(Value.of(IMG_W));
        base.setHeight(Value.of(IMG_H));
        builder.setObject("#VaryonButtonBase.Anchor", base);

        Anchor book = new Anchor();
        book.setLeft(Value.of(0));
        book.setTop(Value.of(0));
        book.setWidth(Value.of(IMG_W));
        book.setHeight(Value.of(IMG_H));
        builder.setObject("#VaryonMenuBook.Anchor", book);

        int keyCx = (int) Math.round((double) KEY_SRC_X * (double) IMG_W / (double) PNG_W);
        int keyCy = (int) Math.round((double) KEY_SRC_Y * (double) IMG_H / (double) PNG_H) + KEY_NUDGE_DOWN_PX;
        int keyLeft = keyCx - KEY_LABEL_W / 2;
        int keyTop = keyCy - KEY_H / 2;
        if (keyLeft < 0) {
            keyLeft = 0;
        } else if (keyLeft > IMG_W - KEY_LABEL_W) {
            keyLeft = IMG_W - KEY_LABEL_W;
        }
        if (keyTop < 0) {
            keyTop = 0;
        } else if (keyTop > IMG_H - KEY_H) {
            keyTop = IMG_H - KEY_H;
        }
        Anchor key = new Anchor();
        key.setLeft(Value.of(keyLeft));
        key.setTop(Value.of(keyTop));
        key.setWidth(Value.of(KEY_LABEL_W));
        key.setHeight(Value.of(KEY_H));
        builder.setObject("#VaryonMenuKey.Anchor", key);
    }
}
