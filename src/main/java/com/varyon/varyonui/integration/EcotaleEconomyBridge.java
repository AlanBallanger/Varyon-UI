package com.varyon.varyonui.integration;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.modules.item.ItemModule;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class EcotaleEconomyBridge {

    private static final Logger LOG = Logger.getLogger("VaryonUI");

    private static final String ECONOMY_BRIDGE = "fr.varyon.ecotale.shared.EconomyBridge";
    private static final String COIN_TYPE_CLASS = "fr.varyon.ecotale.coins.currency.CoinType";
    private static final String ITEM_COIN_LEGACY = "Coin_Copper";
    private static final String ITEM_FALLBACK = "Rock_Stone";

    private static final String BALANCE_ITEM_SLOT = "#SidebarBalanceItemSlot";

    private static Boolean available = null;

    private EcotaleEconomyBridge() {}

    public static boolean isAvailable() {
        if (available == null) {
            try {
                Class.forName(ECONOMY_BRIDGE);
                available = true;
            } catch (ClassNotFoundException e) {
                available = false;
            }
        }
        return Boolean.TRUE.equals(available);
    }

    public static void applySidebarBalance(@Nonnull PlayerRef playerRef, @Nonnull UICommandBuilder ui) {
        if (!isAvailable()) {
            LOG.log(Level.FINE, "[EcotaleSidebar] apply skip: EconomyBridge not available");
            clear(ui);
            return;
        }
        UUID uuid = playerRef.getUuid();
        if (uuid == null) {
            LOG.log(Level.WARNING, "[EcotaleSidebar] apply skip: player UUID null");
            clear(ui);
            return;
        }
        try {
            Class<?> c = Class.forName(ECONOMY_BRIDGE);
            Method getBalance = c.getMethod("getBalance", UUID.class);
            double bal = ((Number) getBalance.invoke(null, uuid)).doubleValue();
            String text = formatAmountOnlyReflect(c, bal);
            ui.set("#SidebarStatBalanceValueMain.TextSpans", Message.raw(text));
            String itemId = pickBalanceItemIdForSlot(true);
            ui.set(BALANCE_ITEM_SLOT + ".ItemId", itemId);
            logItemDiagnostics(itemId);
            LOG.log(Level.INFO, "[EcotaleSidebar] balance uuid=" + uuid + " amountRaw=" + bal + " text=\"" + text
                    + "\" slotSet=" + BALANCE_ITEM_SLOT + ".ItemId itemId=\"" + itemId + "\"");
        } catch (Throwable t) {
            LOG.log(Level.WARNING, "[EcotaleSidebar] apply failed uuid=" + uuid, t);
            clear(ui);
        }
    }

    @Nonnull
    private static String pickBalanceItemIdForSlot(boolean forBalanceDisplay) {
        String[] candidates = balanceItemIdCandidates(forBalanceDisplay);
        String ecotaleResolved = ecotaleCopperItemId();
        LOG.log(Level.INFO, "[EcotaleSidebar] pickItem: ItemModule.exists checks for uuid context, ecotaleCopperId="
                + ecotaleResolved + " candidates=" + String.join(", ", candidates));
        for (String id : candidates) {
            boolean ex = ItemModule.exists(id);
            LOG.log(Level.INFO, "[EcotaleSidebar]   candidate=\"" + id + "\" ItemModule.exists=" + ex);
            if (ex) {
                return id;
            }
        }
        String fallback = candidates[candidates.length - 1];
        LOG.log(Level.WARNING, "[EcotaleSidebar] no candidate had ItemModule.exists==true; using last fallback=\""
                + fallback + "\" (icon may be empty)");
        return fallback;
    }

    private static void logItemDiagnostics(@Nonnull String itemId) {
        try {
            boolean ex = ItemModule.exists(itemId);
            Item asset = Item.getAssetMap().getAsset(itemId);
            String texture = asset != null ? asset.getTexture() : null;
            LOG.log(Level.INFO, "[EcotaleSidebar] diagnostics itemId=\"" + itemId + "\" exists=" + ex
                    + " assetNull=" + (asset == null) + " texture="
                    + (texture == null || texture.isBlank() ? "(empty)" : "\"" + texture + "\""));
        } catch (Throwable t) {
            LOG.log(Level.WARNING, "[EcotaleSidebar] diagnostics failed for itemId=\"" + itemId + "\"", t);
        }
    }

    @Nonnull
    private static String[] balanceItemIdCandidates(boolean forBalanceDisplay) {
        if (!forBalanceDisplay) {
            return new String[]{
                    ITEM_FALLBACK,
                    "Furniture_Crude_Chest_Small",
                    ITEM_COIN_LEGACY
            };
        }
        String ecotaleId = ecotaleCopperItemId();
        if (ecotaleId != null) {
            return new String[]{
                    ecotaleId,
                    ITEM_COIN_LEGACY,
                    ITEM_FALLBACK,
                    "Furniture_Crude_Chest_Small"
            };
        }
        return new String[]{
                ITEM_COIN_LEGACY,
                ITEM_FALLBACK,
                "Furniture_Crude_Chest_Small"
        };
    }

    @Nonnull
    private static String formatAmountOnlyReflect(@Nonnull Class<?> economyBridge, double bal) throws Exception {
        try {
            Method m = economyBridge.getMethod("formatAmountOnly", double.class);
            return (String) m.invoke(null, bal);
        } catch (NoSuchMethodException e) {
            Method legacy = economyBridge.getMethod("format", double.class);
            String withSymbol = (String) legacy.invoke(null, bal);
            return stripLeadingCurrencyNoise(withSymbol);
        }
    }

    @Nonnull
    private static String stripLeadingCurrencyNoise(@Nonnull String formatted) {
        String s = formatted.trim();
        int i = 0;
        while (i < s.length()) {
            char ch = s.charAt(i);
            if (Character.isDigit(ch) || ch == '-' || ch == ',' || ch == '.' || ch == '\u00a0') {
                break;
            }
            i++;
        }
        return i > 0 ? s.substring(i).trim() : s;
    }

    @Nullable
    private static String ecotaleCopperItemId() {
        try {
            Class<?> ct = Class.forName(COIN_TYPE_CLASS);
            Method getItemId = ct.getMethod("getItemId");
            Object[] constants = ct.getEnumConstants();
            if (constants != null) {
                for (Object e : constants) {
                    if (e instanceof Enum<?> en && "COPPER".equals(en.name())) {
                        Object id = getItemId.invoke(e);
                        if (id instanceof String s && !s.isBlank()) {
                            return s;
                        }
                    }
                }
            }
        } catch (Throwable t) {
            LOG.log(Level.FINE, "[EcotaleSidebar] ecotaleCopperItemId reflection failed", t);
        }
        return null;
    }

    private static void clear(@Nonnull UICommandBuilder ui) {
        LOG.log(Level.FINE, "[EcotaleSidebar] clear slot + amount label");
        ui.set("#SidebarStatBalanceValueMain.TextSpans", Message.raw(""));
        ui.setNull(BALANCE_ITEM_SLOT + ".ItemId");
    }
}
