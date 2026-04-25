package com.varyon.varyonui.integration;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.ui.PatchStyle;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.UUID;

public final class CombatProfilBridge {

    private static final String RPG_MAIN = "fr.varyon.rpg.VaryonRPG";
    private static final String WEAPON_UI_ATK = "fr.varyon.rpg.classes.ui.WeaponUiAttackDisplay";

    private static final String ICON_HP = "Icons/health.png";
    private static final String ICON_ATK = "Icons/attack.png";
    private static final String ICON_ARM = "Icons/defense.png";
    private static final String ICON_STA = "Icons/stamina.png";
    private static final String ICON_CRIT_CHANCE = "Icons/taux_crit.png";
    private static final String ICON_CRIT_DAMAGE = "Icons/degat_crit.png";

    private static final PatchStyle CLEAR_ICON_BG =
            new PatchStyle().setColor(Value.of("#00000000"));

    private CombatProfilBridge() {}

    public static void applyCombatProfil(@Nonnull PlayerRef playerRef, Player player, @Nonnull UICommandBuilder ui) {
        Object rpg = getRpgInstance();
        if (rpg == null) {
            clearProfilStats(ui);
            return;
        }
        Object module = invokeNoArg(rpg, "getClassModule");
        if (module == null) {
            clearProfilStats(ui);
            return;
        }
        UUID playerId = playerRef.getUuid();
        Object dataManager = invokeNoArg(module, "getDataManager");
        Object data = invoke(dataManager, "getOrCreate", new Class[]{UUID.class}, new Object[]{playerId});
        if (data == null) {
            clearProfilStats(ui);
            return;
        }
        ui.set("#ProfilStatHPValueMain.TextSpans", Message.raw(String.valueOf(invokeInt(data, "getMaxHp"))));
        ui.set("#ProfilStatHPValuePct.TextSpans", Message.raw(""));

        int weaponDmg = readWeaponDisplayDamage(player);
        ui.set("#ProfilStatATKValueMain.TextSpans", Message.raw(String.valueOf(Math.max(0, weaponDmg))));
        ui.set("#ProfilStatATKValuePct.TextSpans", Message.raw(""));

        ui.set("#ProfilStatArmorValueMain.TextSpans", Message.raw(String.valueOf(invokeInt(data, "getBaseArmor"))));
        ui.set("#ProfilStatArmorValuePct.TextSpans", Message.raw("%"));

        ui.set("#ProfilStatStaminaValueMain.TextSpans", Message.raw(String.valueOf(invokeInt(data, "getMaxStamina"))));
        ui.set("#ProfilStatStaminaValuePct.TextSpans", Message.raw(""));

        ui.set("#ProfilStatCritChanceValueMain.TextSpans", Message.raw(String.valueOf(invokeInt(data, "getCritChancePercent"))));
        ui.set("#ProfilStatCritChanceValuePct.TextSpans", Message.raw("%"));

        ui.set("#ProfilStatCritDamageValueMain.TextSpans", Message.raw("+" + invokeInt(data, "getCritDamageBonusPercent")));
        ui.set("#ProfilStatCritDamageValuePct.TextSpans", Message.raw("%"));

        setIconTexture(ui, "#ProfilStatHPIcon", ICON_HP);
        setIconTexture(ui, "#ProfilStatATKIcon", ICON_ATK);
        setIconTexture(ui, "#ProfilStatArmorIcon", ICON_ARM);
        setIconTexture(ui, "#ProfilStatStaminaIcon", ICON_STA);
        setIconTexture(ui, "#ProfilStatCritChanceIcon", ICON_CRIT_CHANCE);
        setIconTexture(ui, "#ProfilStatCritDamageIcon", ICON_CRIT_DAMAGE);
    }

    private static void clearProfilStats(@Nonnull UICommandBuilder ui) {
        ui.set("#ProfilStatHPValueMain.TextSpans", Message.raw(""));
        ui.set("#ProfilStatHPValuePct.TextSpans", Message.raw(""));
        ui.set("#ProfilStatATKValueMain.TextSpans", Message.raw(""));
        ui.set("#ProfilStatATKValuePct.TextSpans", Message.raw(""));
        ui.set("#ProfilStatArmorValueMain.TextSpans", Message.raw(""));
        ui.set("#ProfilStatArmorValuePct.TextSpans", Message.raw(""));
        ui.set("#ProfilStatStaminaValueMain.TextSpans", Message.raw(""));
        ui.set("#ProfilStatStaminaValuePct.TextSpans", Message.raw(""));
        ui.set("#ProfilStatCritChanceValueMain.TextSpans", Message.raw(""));
        ui.set("#ProfilStatCritChanceValuePct.TextSpans", Message.raw(""));
        ui.set("#ProfilStatCritDamageValueMain.TextSpans", Message.raw(""));
        ui.set("#ProfilStatCritDamageValuePct.TextSpans", Message.raw(""));
        ui.setObject("#ProfilStatHPIcon.Background", CLEAR_ICON_BG);
        ui.setObject("#ProfilStatATKIcon.Background", CLEAR_ICON_BG);
        ui.setObject("#ProfilStatArmorIcon.Background", CLEAR_ICON_BG);
        ui.setObject("#ProfilStatStaminaIcon.Background", CLEAR_ICON_BG);
        ui.setObject("#ProfilStatCritChanceIcon.Background", CLEAR_ICON_BG);
        ui.setObject("#ProfilStatCritDamageIcon.Background", CLEAR_ICON_BG);
    }

    private static void setIconTexture(UICommandBuilder ui, String elementId, String texturePath) {
        ui.setObject(elementId + ".Background", new PatchStyle().setTexturePath(Value.of(texturePath)));
    }

    private static Object getRpgInstance() {
        try {
            Class<?> c = Class.forName(RPG_MAIN);
            Method m = c.getMethod("getRpgInstance");
            return m.invoke(null);
        } catch (Throwable t) {
            return null;
        }
    }

    private static Object invokeNoArg(Object target, String name) {
        if (target == null) return null;
        try {
            Method m = target.getClass().getMethod(name);
            return m.invoke(target);
        } catch (Throwable t) {
            return null;
        }
    }

    private static Object invoke(Object target, String name, Class<?>[] types, Object[] args) {
        if (target == null) return null;
        try {
            Method m = target.getClass().getMethod(name, types);
            return m.invoke(target, args);
        } catch (Throwable t) {
            return null;
        }
    }

    private static boolean invokeBoolean(Object target, String name) {
        if (target == null) return false;
        try {
            Method m = target.getClass().getMethod(name);
            Object r = m.invoke(target);
            return r instanceof Boolean b && b;
        } catch (Throwable t) {
            return false;
        }
    }

    private static int invokeInt(Object target, String name) {
        if (target == null) return 0;
        try {
            Method m = target.getClass().getMethod(name);
            Object r = m.invoke(target);
            if (r instanceof Number n) return n.intValue();
        } catch (Throwable ignored) {
        }
        return 0;
    }

    private static int readWeaponDisplayDamage(Player player) {
        if (player == null) return -1;
        try {
            Class<?> c = Class.forName(WEAPON_UI_ATK);
            Method m = c.getMethod("readHeldWeaponDisplayDamage", Player.class);
            m.setAccessible(true);
            Object r = m.invoke(null, player);
            if (r instanceof Number n) return n.intValue();
        } catch (Throwable ignored) {
        }
        return -1;
    }
}
