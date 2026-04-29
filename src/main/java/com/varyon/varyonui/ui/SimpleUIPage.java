package com.varyon.varyonui.ui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.protocol.packets.interface_.OpenChatWithCommand;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.PatchStyle;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.universe.world.World;
import com.varyon.varyonui.config.AccueilShortcutConfig;
import com.varyon.varyonui.config.MenuShortcutTargetConfig;
import com.varyon.varyonui.config.AdminCommandsConfig;
import com.varyon.varyonui.config.CommandsConfig;
import com.varyon.varyonui.config.NewsConfig;
import com.varyon.varyonui.config.HomeConfig;
import com.varyon.varyonui.config.TutorielConfig;
import com.varyon.varyonui.config.VaryonConfig;
import com.varyon.varyonui.hud.VaryonMenuHud;
import com.varyon.varyonui.integration.CombatProfilBridge;
import com.varyon.varyonui.integration.MenuRpgBridge;
import com.varyon.varyonui.integration.HytlSkinPreview;
import com.varyon.varyonui.VaryonUIPlugin;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleUIPage extends InteractiveCustomUIPage<SimpleUIPage.EventDataClass> {

    private static final long COMMAND_EXECUTE_DELAY_MS = 50L;
    private static final int MAX_SLOTS = 10;
    private static final int MAX_BUTTONS = 50;
    private static final int BUTTONS_PER_ROW = 5;

    private String activeTab;
    private final boolean isAdmin;

    public SimpleUIPage(@Nonnull PlayerRef playerRef) {
        this(playerRef, "home", false);
    }

    public SimpleUIPage(@Nonnull PlayerRef playerRef, @Nonnull String initialTab) {
        this(playerRef, initialTab, false);
    }

    public SimpleUIPage(@Nonnull PlayerRef playerRef, @Nonnull String initialTab, boolean isAdmin) {
        super(playerRef, CustomPageLifetime.CanDismiss, EventDataClass.CODEC);
        this.isAdmin = isAdmin;
        this.activeTab = (initialTab.equals("admin") && !isAdmin) ? "home"
                : "profil".equals(initialTab) ? "home"
                : initialTab;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref,
                      @Nonnull UICommandBuilder commandBuilder,
                      @Nonnull UIEventBuilder eventBuilder,
                      @Nonnull Store<EntityStore> store) {
        commandBuilder.append("VaryonMainPage.ui");
        buildTabBar(commandBuilder, eventBuilder);
        buildContent(commandBuilder, eventBuilder, store, ref);
    }

    private void applyTabIconTextures(@Nonnull UICommandBuilder cb) {
        applyOneTabIcon(cb, "HomeIcon", "Icons/Accueil.png", "Icons/Accueil_Hovered.png", "home");
        applyOneTabIcon(cb, "TutorielIcon", "Icons/Tutoriel.png", "Icons/Tutoriel_Hovered.png", "tutoriel");
        applyOneTabIcon(cb, "CommandesIcon", "Icons/Commandes.png", "Icons/Commandes_Hovered.png", "commandes");
        applyOneTabIcon(cb, "MisesAJourIcon", "Icons/Mises_a_jour.png", "Icons/Mises_a_jour_Hovered.png", "misesajour");
        applyOneTabIcon(cb, "VaryonIcon", "Icons/Varyon.png", "Icons/Varyon_Hovered.png", "varyon");
        applyOneTabIcon(cb, "ParametresIcon", "Icons/Infos.png", "Icons/Infos_Hovered.png", "parametres");
        if (isAdmin) {
            applyOneTabIcon(cb, "AdminIcon", "Icons/Commandes.png", "Icons/Commandes_Hovered.png", "admin");
        }
    }

    private void applyOneTabIcon(
            @Nonnull UICommandBuilder cb,
            @Nonnull String iconId,
            @Nonnull String normalPath,
            @Nonnull String hoveredPath,
            @Nonnull String tabKey) {
        boolean useHovered = tabKey.equals(activeTab);
        String path = useHovered ? hoveredPath : normalPath;
        PatchStyle ps =
            new PatchStyle().setTexturePath(Value.of(normalizeIconUrl(path))).setBorder(Value.of(0));
        cb.setObject("#" + iconId + ".Background", ps);
    }

    private void buildTabBar(@Nonnull UICommandBuilder commandBuilder,
                             @Nonnull UIEventBuilder eventBuilder) {
        patchTabBarAppearance(commandBuilder);
        appendTabBarEvents(commandBuilder, eventBuilder);
        HytlSkinPreview.applyPlaceholder(commandBuilder);
        scheduleSkinHeadshotFetch();
    }

    private void patchTabBarAppearance(@Nonnull UICommandBuilder commandBuilder) {
        commandBuilder.set("#AdminTabContainer.Visible", isAdmin);

        applyTabIconTextures(commandBuilder);
        applyTabUnderlineVisibility(commandBuilder);
    }

    private void applyTabUnderlineVisibility(@Nonnull UICommandBuilder commandBuilder) {
        commandBuilder.set("#HomeTabUnderline.Visible", "home".equals(activeTab));
        commandBuilder.set("#TutorielTabUnderline.Visible", "tutoriel".equals(activeTab));
        commandBuilder.set("#CommandesTabUnderline.Visible", "commandes".equals(activeTab));
        commandBuilder.set("#MisesAJourTabUnderline.Visible", "misesajour".equals(activeTab));
        commandBuilder.set("#VaryonTabUnderline.Visible", "varyon".equals(activeTab));
        commandBuilder.set("#ParametresTabUnderline.Visible", "parametres".equals(activeTab));
        if (isAdmin) {
            commandBuilder.set("#AdminTabUnderline.Visible", "admin".equals(activeTab));
        }
    }

    private void appendTabBarEvents(
            @Nonnull UICommandBuilder commandBuilder,
            @Nonnull UIEventBuilder eventBuilder) {
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#HomeTab", EventData.of("Action", "tab").append("Tab", "home"));
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#TutorielTab", EventData.of("Action", "tab").append("Tab", "tutoriel"));
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#CommandesTab", EventData.of("Action", "tab").append("Tab", "commandes"));
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#MisesAJourTab", EventData.of("Action", "tab").append("Tab", "misesajour"));
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#VaryonTab", EventData.of("Action", "tab").append("Tab", "varyon"));
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#ParametresTab", EventData.of("Action", "tab").append("Tab", "parametres"));
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#CloseButton", EventData.of("Action", "close"));

        eventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#MenuGenDetailBtn",
            EventData.of("Action", "chatcommand").append("Command", "/vrpg")
        );
        eventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#MenuJob1DetailBtn",
            EventData.of("Action", "chatcommand").append("Command", "/vp")
        );
        eventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#MenuJob2DetailBtn",
            EventData.of("Action", "chatcommand").append("Command", "/vp")
        );

        eventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#ParametresMenuShortcutAlt",
            EventData.of("Action", "accueilshortcut").append("ShortcutMode", "alt")
        );
        eventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#ParametresMenuShortcutO",
            EventData.of("Action", "accueilshortcut").append("ShortcutMode", "o")
        );
        eventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#ParametresMenuShortcutDisable",
            EventData.of("Action", "accueilshortcut").append("ShortcutMode", "disable")
        );

        eventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#ParametresMenuTargetAccueil",
            EventData.of("Action", "menushortcuttarget").append("MenuShortcutTarget", "accueil")
        );
        eventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#ParametresMenuTargetCommandes",
            EventData.of("Action", "menushortcuttarget").append("MenuShortcutTarget", "commandes")
        );
        eventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#ParametresMenuTargetVaryon",
            EventData.of("Action", "menushortcuttarget").append("MenuShortcutTarget", "varyon")
        );

        if (isAdmin) {
            eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#AdminTab", EventData.of("Action", "tab").append("Tab", "admin"));
        }

        if ("commandes".equals(activeTab)) {
            buildCommandButtons(commandBuilder, eventBuilder);
        }
        if ("varyon".equals(activeTab)) {
            buildVaryonQuickButtonEvents(eventBuilder);
        }
        if ("admin".equals(activeTab) && isAdmin) {
            buildAdminCommandButtons(commandBuilder, eventBuilder);
        }
    }

    private void scheduleSkinHeadshotFetch() {
        UUID uuid = playerRef.getUuid();
        if (uuid == null) {
            return;
        }
        HytaleServer.SCHEDULED_EXECUTOR.execute(() -> {
            byte[] png = HytlSkinPreview.fetchHeadshotPng(uuid);
            Ref<EntityStore> ref = playerRef.getReference();
            if (ref == null || !ref.isValid()) {
                return;
            }
            UICommandBuilder cb = new UICommandBuilder();
            UIEventBuilder eb = new UIEventBuilder();
            patchTabBarAppearance(cb);
            appendTabBarEvents(cb, eb);
            HytlSkinPreview.applyPngToPreview(cb, uuid, png, VaryonUIPlugin.getInstance());
            sendUpdate(cb, eb, false);
        });
    }

    private static void buildVaryonQuickButtonEvents(@Nonnull UIEventBuilder eventBuilder) {
        for (int i = 0; i < VARYON_QUICK_COMMANDS.length; i++) {
            int n = i + 1;
            String action = VARYON_QUICK_CHAT[i] ? "chatcommand" : "command";
            eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#VaryonQuickButton" + n,
                EventData.of("Action", action).append("Command", VARYON_QUICK_COMMANDS[i])
            );
        }
    }

    private static final String[] VARYON_QUICK_COMMANDS = {
        "/extract",
        "/return",
        "/join joueur ",
        "/rtpv 1",
        "/essence",
    };

    private static final boolean[] VARYON_QUICK_CHAT = {
        false,
        false,
        true,
        false,
        false,
    };

    private static final String[] VARYON_QUICK_LABELS = {
        "Extraction",
        "Retour mort",
        "Rejoindre ami",
        "TP aléatoire",
        "Qté essence",
    };

    private void buildCommandButtons(@Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder) {
        List<CommandsConfig.CommandCategory> categories = CommandsConfig.getInstance().getCategories();
        int slotIndex = 0;
        for (CommandsConfig.CommandCategory category : categories) {
            List<CommandsConfig.CommandButton> buttons = category.getButtons();
            for (int i = 0; i < buttons.size(); i += BUTTONS_PER_ROW) {
                slotIndex++;
                if (slotIndex > MAX_SLOTS) return;
                int baseIdx = (slotIndex - 1) * BUTTONS_PER_ROW;
                int count = Math.min(BUTTONS_PER_ROW, buttons.size() - i);
                for (int j = 0; j < count; j++) {
                    int btnIdx = baseIdx + j + 1;
                    if (btnIdx > MAX_BUTTONS) return;
                    CommandsConfig.CommandButton button = buttons.get(i + j);
                    String action = button.getType() == CommandsConfig.CommandType.CHAT ? "chatcommand" : "command";
                    eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#CommandButton" + btnIdx, EventData.of("Action", action).append("Command", button.getCommand()));
                }
            }
        }
    }

    private void buildAdminCommandButtons(@Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder) {
        List<CommandsConfig.CommandCategory> categories = AdminCommandsConfig.getInstance().getCategories();
        int listIdx = 0;
        for (CommandsConfig.CommandCategory category : categories) {
            List<CommandsConfig.CommandButton> buttons = category.getButtons();
            if (buttons.isEmpty()) continue;
            listIdx++;
            for (int i = 0; i < buttons.size(); i += BUTTONS_PER_ROW) {
                int rowListIdx = listIdx;
                listIdx++;
                int count = Math.min(BUTTONS_PER_ROW, buttons.size() - i);
                for (int j = 0; j < count; j++) {
                    CommandsConfig.CommandButton button = buttons.get(i + j);
                    String action = button.getType() == CommandsConfig.CommandType.CHAT ? "chatcommand" : "command";
                    eventBuilder.addEventBinding(
                        CustomUIEventBindingType.Activating,
                        "#AdminCommandesList[" + rowListIdx + "][" + j + "]",
                        EventData.of("Action", action).append("Command", button.getCommand())
                    );
                }
            }
        }
    }

    private void buildContent(@Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref) {
        commandBuilder.set("#HomeContent.Visible", "home".equals(activeTab));
        commandBuilder.set("#TutorielContent.Visible", "tutoriel".equals(activeTab));
        commandBuilder.set("#CommandesContent.Visible", "commandes".equals(activeTab));
        commandBuilder.set("#MisesAJourContent.Visible", "misesajour".equals(activeTab));
        commandBuilder.set("#VaryonContent.Visible", "varyon".equals(activeTab));
        commandBuilder.set("#ParametresContent.Visible", "parametres".equals(activeTab));
        commandBuilder.set("#AdminContent.Visible", "admin".equals(activeTab) && isAdmin);

        if ("commandes".equals(activeTab)) {
            buildCommandsContent(commandBuilder);
        }
        if ("admin".equals(activeTab) && isAdmin) {
            buildAdminCommandsContent(commandBuilder);
        }
        if ("misesajour".equals(activeTab)) {
            buildNewsContent(commandBuilder);
        }
        if ("tutoriel".equals(activeTab)) {
            buildTutorielContent(commandBuilder);
        }
        if ("varyon".equals(activeTab)) {
            buildVaryonContent(commandBuilder);
        }
        if ("home".equals(activeTab)) {
            buildHomeContent(commandBuilder);
        }
        Player player = store.getComponent(ref, Player.getComponentType());
        CombatProfilBridge.applyCombatProfil(playerRef, player, commandBuilder);
        MenuRpgBridge.applyMenuXp(playerRef.getUuid(), commandBuilder);
        if ("parametres".equals(activeTab)) {
            PlayerRef pref = store.getComponent(ref, PlayerRef.getComponentType());
            applyParametresShortcutAppearance(commandBuilder, pref != null ? pref.getUuid() : null);
        }
    }

    private static void applyParametresShortcutAppearance(
            @Nonnull UICommandBuilder commandBuilder,
            @Nullable UUID uuid) {
        AccueilShortcutConfig.Mode mode = uuid == null
            ? AccueilShortcutConfig.Mode.defaultMode()
            : AccueilShortcutConfig.getInstance().getMode(uuid);
        applyShortcutToggle(commandBuilder, "ParametresMenuShortcutAlt", "ParametresMenuShortcutAltLabel",
            mode == AccueilShortcutConfig.Mode.ALT);
        applyShortcutToggle(commandBuilder, "ParametresMenuShortcutO", "ParametresMenuShortcutOLabel",
            mode == AccueilShortcutConfig.Mode.O);
        applyShortcutToggle(commandBuilder, "ParametresMenuShortcutDisable", "ParametresMenuShortcutDisableLabel",
            mode == AccueilShortcutConfig.Mode.DISABLE);
        MenuShortcutTargetConfig.Target menuTarget = uuid == null
            ? MenuShortcutTargetConfig.Target.defaultTarget()
            : MenuShortcutTargetConfig.getInstance().getTarget(uuid);
        applyShortcutToggle(commandBuilder, "ParametresMenuTargetAccueil", "ParametresMenuTargetAccueilLabel",
            menuTarget == MenuShortcutTargetConfig.Target.ACCUEIL);
        applyShortcutToggle(commandBuilder, "ParametresMenuTargetCommandes", "ParametresMenuTargetCommandesLabel",
            menuTarget == MenuShortcutTargetConfig.Target.COMMANDES);
        applyShortcutToggle(commandBuilder, "ParametresMenuTargetVaryon", "ParametresMenuTargetVaryonLabel",
            menuTarget == MenuShortcutTargetConfig.Target.VARYON);
    }

    private static void applyShortcutToggle(
            @Nonnull UICommandBuilder cb,
            @Nonnull String btnId,
            @Nonnull String labelId,
            boolean selected) {
        String selBg = "#356cb0";
        String idleBg = "#1a2838";
        String selHov = "#447ccd";
        String idleHov = "#243448";
        String bg = selected ? selBg : idleBg;
        String hov = selected ? selHov : idleHov;
        PatchStyle def = new PatchStyle().setColor(Value.of(bg));
        PatchStyle hovS = new PatchStyle().setColor(Value.of(hov));
        String base = "#" + btnId + ".Style";
        cb.setObject(base + ".Default.Background", def);
        cb.setObject(base + ".Hovered.Background", hovS);
        cb.setObject(base + ".Pressed.Background", def);
        cb.set("#" + labelId + ".Style.TextColor", selected ? "#e8f4ff" : "#8899aa");
    }

    private void buildCommandsContent(@Nonnull UICommandBuilder commandBuilder) {
        List<CommandsConfig.CommandCategory> categories = CommandsConfig.getInstance().getCategories();

        for (int i = 1; i <= MAX_SLOTS; i++) {
            commandBuilder.set("#CategoryHeader" + i + ".Visible", false);
            commandBuilder.set("#ButtonRow" + i + ".Visible", false);
        }
        for (int i = 1; i <= MAX_BUTTONS; i++) {
            commandBuilder.set("#CommandButton" + i + ".Visible", false);
        }

        int slotIndex = 0;

        for (CommandsConfig.CommandCategory category : categories) {
            List<CommandsConfig.CommandButton> buttons = category.getButtons();
            boolean isFirstRow = true;

            for (int i = 0; i < buttons.size(); i += BUTTONS_PER_ROW) {
                slotIndex++;
                if (slotIndex > MAX_SLOTS) break;

                if (isFirstRow) {
                    String catName = category.getName();
                    if (catName != null && !catName.isBlank()) {
                        commandBuilder.set("#CategoryHeader" + slotIndex + ".Visible", true);
                        commandBuilder.set("#CategoryTitle" + slotIndex + ".TextSpans", Message.raw(catName));
                        applyCategoryHeaderIcon(commandBuilder, slotIndex, category.getHeaderIcon());
                    }
                    isFirstRow = false;
                }

                commandBuilder.set("#ButtonRow" + slotIndex + ".Visible", true);

                int baseIdx = (slotIndex - 1) * BUTTONS_PER_ROW;
                int count = Math.min(BUTTONS_PER_ROW, buttons.size() - i);
                for (int j = 0; j < count; j++) {
                    int btnIdx = baseIdx + j + 1;
                    if (btnIdx > MAX_BUTTONS) break;
                    CommandsConfig.CommandButton button = buttons.get(i + j);
                    commandBuilder.set("#CommandButton" + btnIdx + ".Visible", true);
                    commandBuilder.set("#CommandButton" + btnIdx + "Label.TextSpans", Message.raw(button.getLabel()));
                    commandBuilder.set("#CommandButton" + btnIdx + "Cmd.TextSpans", Message.raw(button.getCommand()));
                    applyCommandButtonIconBackground(commandBuilder, btnIdx, button.getIconUrl());
                    applyCommandButtonSolidBackground(commandBuilder, btnIdx);
                }
            }
        }
    }

    private static final String COMMAND_CATEGORY_HEADER_ICON_COLOR = "#243548";
    private static final String COMMAND_BUTTON_ICON_SLOT_COLOR = "#1e3348";
    private static final String COMMAND_BUTTON_DEFAULT_COLOR = "#2a4a6a";
    private static final String COMMAND_BUTTON_HOVERED_COLOR = "#3a5a7a";

    private static void applySolidChromeButtonStyle(
            @Nonnull UICommandBuilder commandBuilder, @Nonnull String buttonSelector) {
        PatchStyle def = new PatchStyle().setColor(Value.of(COMMAND_BUTTON_DEFAULT_COLOR));
        PatchStyle hov = new PatchStyle().setColor(Value.of(COMMAND_BUTTON_HOVERED_COLOR));
        String base = buttonSelector + ".Style";
        commandBuilder.setObject(base + ".Default.Background", def);
        commandBuilder.setObject(base + ".Hovered.Background", hov);
        commandBuilder.setObject(base + ".Pressed.Background", hov);
    }

    private static void applyCommandButtonSolidBackground(@Nonnull UICommandBuilder commandBuilder, int btnIdx) {
        applySolidChromeButtonStyle(commandBuilder, "#CommandButton" + btnIdx);
    }

    private static boolean commandButtonHasIconSlot(int btnIdx) {
        return btnIdx >= 1 && btnIdx <= 35;
    }

    private static void applyCategoryHeaderIcon(
            @Nonnull UICommandBuilder commandBuilder, int slotIndex, @Nullable String headerIcon) {
        String path = headerIcon != null ? headerIcon.trim() : "";
        if (!path.isEmpty()) {
            PatchStyle style = new PatchStyle().setTexturePath(Value.of(normalizeIconUrl(path)));
            commandBuilder.setObject("#CategoryHeader" + slotIndex + "Icon.Background", style);
        } else {
            commandBuilder.setObject(
                "#CategoryHeader" + slotIndex + "Icon.Background",
                new PatchStyle().setColor(Value.of(COMMAND_CATEGORY_HEADER_ICON_COLOR))
            );
        }
    }

    private static void applyCommandButtonIconBackground(
            @Nonnull UICommandBuilder commandBuilder, int btnIdx, @Nullable String iconUrl) {
        if (!commandButtonHasIconSlot(btnIdx)) {
            return;
        }
        String path = iconUrl != null ? iconUrl.trim() : "";
        if (!path.isEmpty()) {
            PatchStyle style = new PatchStyle().setTexturePath(Value.of(normalizeIconUrl(path)));
            commandBuilder.setObject("#CommandButton" + btnIdx + "Icon.Background", style);
            return;
        }
        if (btnIdx > 5) {
            commandBuilder.setObject(
                "#CommandButton" + btnIdx + "Icon.Background",
                new PatchStyle().setColor(Value.of(COMMAND_BUTTON_ICON_SLOT_COLOR))
            );
        }
    }

    private static String normalizeIconUrl(String raw) {
        if (raw.startsWith("/")) {
            return raw.substring(1);
        }
        return raw;
    }

    private static final String ADMIN_BTN_STYLE =
        "Background: (Color: #2a4a6a); Style: ButtonStyle(" +
        "Default: (Background: (Color: #2a4a6a)), " +
        "Hovered: (Background: (Color: #3a5a7a)), " +
        "Pressed: (Background: (Color: #2a4a6a)));";

    private void buildAdminCommandsContent(@Nonnull UICommandBuilder commandBuilder) {
        commandBuilder.clear("#AdminCommandesList");

        List<CommandsConfig.CommandCategory> categories = AdminCommandsConfig.getInstance().getCategories();
        int listIdx = 0;

        for (CommandsConfig.CommandCategory category : categories) {
            List<CommandsConfig.CommandButton> buttons = category.getButtons();
            if (buttons.isEmpty()) continue;

            String titleText = escapeForUI(category.getName().toUpperCase());
            commandBuilder.appendInline("#AdminCommandesList",
                "Label { Text: \"" + titleText + "\"; Style: (FontSize: 20, RenderBold: true, TextColor: #aaddff); Anchor: (Top: 8, Bottom: 8); }");
            listIdx++;

            for (int i = 0; i < buttons.size(); i += BUTTONS_PER_ROW) {
                int rowListIdx = listIdx;
                commandBuilder.appendInline("#AdminCommandesList",
                    "Group { LayoutMode: Left; Anchor: (Height: 53, Bottom: 4); }");
                listIdx++;

                int count = Math.min(BUTTONS_PER_ROW, buttons.size() - i);
                for (int j = 0; j < count; j++) {
                    CommandsConfig.CommandButton button = buttons.get(i + j);
                    String btnLabel = escapeForUI(button.getLabel());
                    String btnCmd = escapeForUI(button.getCommand());
                    boolean isLast = (j == count - 1);
                    String anchor = isLast ?
                        "Anchor: (Width: 152, Height: 53);" :
                        "Anchor: (Width: 152, Height: 53, Right: 12);";
                    commandBuilder.appendInline("#AdminCommandesList[" + rowListIdx + "]",
                        "Button { " + anchor + " " + ADMIN_BTN_STYLE +
                        " Group { LayoutMode: Top; Padding: (Top: 6, Bottom: 6, Left: 6, Right: 6);" +
                        " Label { Text: \"" + btnLabel + "\"; Style: (FontSize: 14, TextColor: #ffffff, RenderBold: true, HorizontalAlignment: Center); Anchor: (Bottom: 2); }" +
                        " Label { Text: \"" + btnCmd + "\"; Style: (FontSize: 10, TextColor: #aaaaaa, HorizontalAlignment: Center); }" +
                        " } }");
                }
            }
        }
    }

    private static final int MAX_NEWS = 10;

    private static final String KW_GREEN = "#66bb6a";
    private static final String KW_PURPLE = "#ab47bc";
    private static final String KW_ORANGE = "#ffa726";
    private static final String KW_BLUE = "#42a5f5";

    private static final Pattern KEYWORD_PATTERN = Pattern.compile(
        "(?iu)\\b(Fraktale|Fracture|Haven|Gaïa|Gaia|Varyon|Novale|Noyau)\\b");

    private static Message buildRichSegment(@Nonnull String text, boolean boldPlainParts) {
        if (text.isEmpty()) {
            return Message.raw("");
        }
        Matcher m = KEYWORD_PATTERN.matcher(text);
        Message out = Message.empty();
        int last = 0;
        while (m.find()) {
            if (m.start() > last) {
                Message plain = Message.raw(text.substring(last, m.start()));
                if (boldPlainParts) {
                    plain = plain.bold(true);
                }
                out.insert(plain);
            }
            String matched = text.substring(m.start(), m.end());
            out.insert(Message.raw(matched).color(keywordColor(matched)).bold(true));
            last = m.end();
        }
        if (last < text.length()) {
            Message plain = Message.raw(text.substring(last));
            if (boldPlainParts) {
                plain = plain.bold(true);
            }
            out.insert(plain);
        }
        return out;
    }

    private static Message buildLineWithBoldMarkup(@Nonnull String line) {
        Message out = Message.empty();
        int pos = 0;
        while (true) {
            int b = line.indexOf("[B]", pos);
            if (b < 0) {
                out.insert(buildRichSegment(line.substring(pos), false));
                break;
            }
            if (b > pos) {
                out.insert(buildRichSegment(line.substring(pos, b), false));
            }
            int close = line.indexOf("[/B]", b + 3);
            if (close < 0) {
                out.insert(buildRichSegment(line.substring(b), false));
                break;
            }
            String inner = line.substring(b + 3, close);
            out.insert(buildRichSegment(inner, true));
            pos = close + 4;
        }
        return out;
    }

    private static String keywordColor(@Nonnull String word) {
        String w = word.toLowerCase(Locale.ROOT);
        return switch (w) {
            case "haven", "gaïa", "gaia" -> KW_GREEN;
            case "varyon" -> KW_PURPLE;
            case "fracture", "fraktale" -> KW_ORANGE;
            case "noyau", "novale" -> KW_BLUE;
            default -> "#dddddd";
        };
    }

    private static void appendRichLabelLine(
        @Nonnull UICommandBuilder commandBuilder,
        @Nonnull String containerId,
        @Nonnull String lineIdPrefix,
        @Nonnull int[] lineCounter,
        @Nonnull Message textSpans,
        int fontSize,
        @Nonnull String defaultTextColor,
        boolean bold,
        boolean useBottomAnchor
    ) {
        String id = lineIdPrefix + lineCounter[0]++;
        String anchor = useBottomAnchor ? "Anchor: (Bottom: 4, Left: 0, Right: 0);" : "Anchor: (Left: 0, Right: 0);";
        commandBuilder.appendInline(
            containerId,
            "Label #" + id + " { " + anchor + " Style: (FontSize: " + fontSize + ", TextColor: " + defaultTextColor + ", RenderBold: " + bold + ", Wrap: true); }");
        commandBuilder.set("#" + id + ".TextSpans", textSpans);
    }

    private void buildScrollableRichContent(
        @Nonnull UICommandBuilder commandBuilder,
        @Nonnull String containerId,
        @Nonnull String content,
        @Nonnull String lineIdPrefix,
        boolean useBottomAnchorOnLabels
    ) {
        commandBuilder.clear(containerId);
        int[] nextLine = {0};
        String[] lines = content.split("\\R");
        boolean previousWasTitle = false;

        for (String rawLine : lines) {
            String line = rawLine.trim();

            if (line.equals("[SEPARATOR]")) {
                commandBuilder.appendInline(containerId, "Group { Anchor: (Height: 8); }");
                commandBuilder.appendInline(containerId, "Group { Anchor: (Height: 1, Left: 0, Right: 0); Background: (Color: #4a5568); }");
                commandBuilder.appendInline(containerId, "Group { Anchor: (Height: 8); }");
                previousWasTitle = false;
            } else if (line.isEmpty()) {
                previousWasTitle = false;
            } else if (line.startsWith("[COLOR:") && line.contains("]")) {
                if (!previousWasTitle) {
                    commandBuilder.appendInline(containerId, "Group { Anchor: (Height: 8); }");
                }
                int colorEnd = line.indexOf("]");
                String titleText = line.substring(colorEnd + 1).replace("[/COLOR]", "");
                appendRichLabelLine(
                    commandBuilder,
                    containerId,
                    lineIdPrefix,
                    nextLine,
                    buildLineWithBoldMarkup(titleText),
                    18,
                    "#ffffff",
                    true,
                    useBottomAnchorOnLabels);
                commandBuilder.appendInline(containerId, "Group { Anchor: (Height: 4); }");
                previousWasTitle = true;
            } else {
                appendRichLabelLine(
                    commandBuilder,
                    containerId,
                    lineIdPrefix,
                    nextLine,
                    buildLineWithBoldMarkup(line),
                    14,
                    "#dddddd",
                    false,
                    useBottomAnchorOnLabels);
                previousWasTitle = false;
            }
        }
    }

    private void buildHomeContent(@Nonnull UICommandBuilder commandBuilder) {
        buildScrollableRichContent(
            commandBuilder,
            "#HomeTextContainer",
            HomeConfig.getInstance().getContent(),
            "HomeLn",
            false);
    }
    
    private String escapeForUI(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private void buildNewsContent(@Nonnull UICommandBuilder commandBuilder) {
        List<NewsConfig.NewsEntry> entries = NewsConfig.getInstance().getEntries();

        for (int i = 1; i <= MAX_NEWS; i++) {
            commandBuilder.set("#NewsEntry" + i + ".Visible", false);
        }

        for (int i = 0; i < entries.size() && i < MAX_NEWS; i++) {
            int idx = i + 1;
            NewsConfig.NewsEntry entry = entries.get(i);
            commandBuilder.set("#NewsEntry" + idx + ".Visible", true);
            commandBuilder.set("#NewsDate" + idx + ".TextSpans", Message.raw(entry.getDate()));
            commandBuilder.set("#NewsTitle" + idx + ".TextSpans", Message.raw(entry.getTitle()));
            commandBuilder.set("#NewsContent" + idx + ".TextSpans", Message.raw(entry.getContent()));
        }
    }

    private void buildTutorielContent(@Nonnull UICommandBuilder commandBuilder) {
        buildScrollableRichContent(
            commandBuilder,
            "#TutorielTextContainer",
            TutorielConfig.getInstance().getContent(),
            "TutoLn",
            true);
    }

    private void buildVaryonContent(@Nonnull UICommandBuilder commandBuilder) {
        buildVaryonQuickRow(commandBuilder);
        buildScrollableTextContent(commandBuilder, "#VaryonTextContainer", VaryonConfig.getInstance().getContent());
    }

    private static final String VARYON_QUICK_ICON = "Icons/Varyon_Icon.png";

    private void buildVaryonQuickRow(@Nonnull UICommandBuilder commandBuilder) {
        PatchStyle iconStyle = new PatchStyle().setTexturePath(Value.of(normalizeIconUrl(VARYON_QUICK_ICON)));
        for (int i = 0; i < VARYON_QUICK_LABELS.length; i++) {
            int n = i + 1;
            applySolidChromeButtonStyle(commandBuilder, "#VaryonQuickButton" + n);
            commandBuilder.set("#VaryonQuickButton" + n + "Label.TextSpans", Message.raw(VARYON_QUICK_LABELS[i]));
            commandBuilder.set("#VaryonQuickButton" + n + "Cmd.TextSpans", Message.raw(VARYON_QUICK_COMMANDS[i].trim()));
            commandBuilder.setObject("#VaryonQuickButton" + n + "Icon.Background", iconStyle);
        }
    }

    private void buildScrollableTextContent(@Nonnull UICommandBuilder commandBuilder, @Nonnull String containerId, @Nonnull String content) {
        commandBuilder.clear(containerId);

        String[] lines = content.split("\\R");
        boolean previousWasTitle = false;

        for (String line : lines) {
            line = line.trim();

            if (line.equals("[SEPARATOR]")) {
                commandBuilder.appendInline(containerId, "Group { Anchor: (Height: 8); }");
                commandBuilder.appendInline(containerId, "Group { Anchor: (Height: 1, Left: 0, Right: 0); Background: (Color: #4a5568); }");
                commandBuilder.appendInline(containerId, "Group { Anchor: (Height: 8); }");
                previousWasTitle = false;
            } else if (line.isEmpty()) {
                previousWasTitle = false;
            } else if (line.startsWith("[COLOR:") && line.contains("]")) {
                if (!previousWasTitle) {
                    commandBuilder.appendInline(containerId, "Group { Anchor: (Height: 8); }");
                }
                int colorEnd = line.indexOf("]");
                String colorCode = line.substring(7, colorEnd);
                String text = line.substring(colorEnd + 1).replace("[/COLOR]", "");
                text = escapeForUI(text);
                commandBuilder.appendInline(containerId, "Label { Text: \"" + text + "\"; Style: (FontSize: 16, TextColor: " + colorCode + ", RenderBold: true, Wrap: true); Anchor: (Bottom: 4, Left: 0, Right: 0); }");
                commandBuilder.appendInline(containerId, "Group { Anchor: (Height: 4); }");
                previousWasTitle = true;
            } else {
                String text = escapeForUI(line);
                commandBuilder.appendInline(containerId, "Label { Text: \"" + text + "\"; Style: (FontSize: 14, TextColor: #dddddd, Wrap: true); Anchor: (Bottom: 4, Left: 0, Right: 0); }");
                previousWasTitle = false;
            }
        }
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull EventDataClass data) {
        if ("tab".equals(data.action) && data.tab != null) {
            if ("admin".equals(data.tab) && !isAdmin) {
                return;
            }
            activeTab = data.tab;
            UICommandBuilder commandBuilder = new UICommandBuilder();
            UIEventBuilder eventBuilder = new UIEventBuilder();
            buildTabBar(commandBuilder, eventBuilder);
            buildContent(commandBuilder, eventBuilder, store, ref);
            sendUpdate(commandBuilder, eventBuilder, false);
        } else if ("close".equals(data.action)) {
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player != null) {
                player.getPageManager().setPage(ref, store, Page.None);
            }
        } else if ("accueilshortcut".equals(data.action) && data.shortcutMode != null) {
            PlayerRef playerRefComp = store.getComponent(ref, PlayerRef.getComponentType());
            Player playerComp = store.getComponent(ref, Player.getComponentType());
            if (playerRefComp != null && playerRefComp.getUuid() != null) {
                AccueilShortcutConfig.Mode m = AccueilShortcutConfig.Mode.fromKey(data.shortcutMode);
                AccueilShortcutConfig.getInstance().setMode(playerRefComp.getUuid(), m);
                VaryonMenuHud.attach(playerComp, playerRefComp);
                UICommandBuilder commandBuilder = new UICommandBuilder();
                UIEventBuilder eventBuilder = new UIEventBuilder();
                buildTabBar(commandBuilder, eventBuilder);
                buildContent(commandBuilder, eventBuilder, store, ref);
                sendUpdate(commandBuilder, eventBuilder, false);
            }
        } else if ("menushortcuttarget".equals(data.action) && data.menuShortcutTarget != null) {
            PlayerRef playerRefComp = store.getComponent(ref, PlayerRef.getComponentType());
            if (playerRefComp != null && playerRefComp.getUuid() != null) {
                MenuShortcutTargetConfig.Target t =
                    MenuShortcutTargetConfig.Target.fromKey(data.menuShortcutTarget);
                MenuShortcutTargetConfig.getInstance().setTarget(playerRefComp.getUuid(), t);
                UICommandBuilder commandBuilder = new UICommandBuilder();
                UIEventBuilder eventBuilder = new UIEventBuilder();
                buildTabBar(commandBuilder, eventBuilder);
                buildContent(commandBuilder, eventBuilder, store, ref);
                sendUpdate(commandBuilder, eventBuilder, false);
            }
        } else if ("chatcommand".equals(data.action) && data.command != null) {
            Player player = store.getComponent(ref, Player.getComponentType());
            PlayerRef playerRefComponent = store.getComponent(ref, PlayerRef.getComponentType());
            if (player != null && playerRefComponent != null) {
                player.getPageManager().setPage(ref, store, Page.None);
                playerRefComponent.getPacketHandler().write(new OpenChatWithCommand(data.command));
            }
        } else if ("command".equals(data.action) && data.command != null) {
            Player player = store.getComponent(ref, Player.getComponentType());
            PlayerRef playerRefComponent = store.getComponent(ref, PlayerRef.getComponentType());
            if (player != null && playerRefComponent != null) {
                player.getPageManager().setPage(ref, store, Page.None);
                String command = data.command.startsWith("/") ? data.command.substring(1) : data.command;
                String cmd = command;
                PlayerRef pref = playerRefComponent;
                World world = player.getWorld();
                Runnable runCommand = () -> CommandManager.get().handleCommand(pref, cmd);
                if (world != null) {
                    HytaleServer.SCHEDULED_EXECUTOR.schedule(
                        () -> world.execute(runCommand),
                        COMMAND_EXECUTE_DELAY_MS,
                        TimeUnit.MILLISECONDS
                    );
                } else {
                    HytaleServer.SCHEDULED_EXECUTOR.schedule(runCommand, COMMAND_EXECUTE_DELAY_MS, TimeUnit.MILLISECONDS);
                }
            }
        }
    }

    public static class EventDataClass {
        public static final BuilderCodec<EventDataClass> CODEC =
            BuilderCodec.builder(EventDataClass.class, EventDataClass::new)
                .addField(new KeyedCodec<>("Action", Codec.STRING), (entry, s) -> entry.action = s, entry -> entry.action)
                .addField(new KeyedCodec<>("Tab", Codec.STRING), (entry, s) -> entry.tab = s, entry -> entry.tab)
                .addField(new KeyedCodec<>("Command", Codec.STRING), (entry, s) -> entry.command = s, entry -> entry.command)
                .addField(new KeyedCodec<>("ShortcutMode", Codec.STRING), (entry, s) -> entry.shortcutMode = s, entry -> entry.shortcutMode)
                .addField(new KeyedCodec<>("MenuShortcutTarget", Codec.STRING), (entry, s) -> entry.menuShortcutTarget = s, entry -> entry.menuShortcutTarget)
                .build();

        public String action;
        public String tab;
        public String command;
        public String shortcutMode;
        public String menuShortcutTarget;
    }
}
