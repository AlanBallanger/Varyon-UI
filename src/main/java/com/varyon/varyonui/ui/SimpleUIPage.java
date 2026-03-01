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
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.varyon.varyonui.config.AdminCommandsConfig;
import com.varyon.varyonui.config.CommandsConfig;
import com.varyon.varyonui.config.NewsConfig;
import com.varyon.varyonui.config.HomeConfig;

import javax.annotation.Nonnull;
import java.util.List;

public class SimpleUIPage extends InteractiveCustomUIPage<SimpleUIPage.EventDataClass> {

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
        this.activeTab = (initialTab.equals("admin") && !isAdmin) ? "home" : initialTab;
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

    private void buildTabBar(@Nonnull UICommandBuilder commandBuilder,
                             @Nonnull UIEventBuilder eventBuilder) {
        commandBuilder.set("#HomeTab.Style.Default.Background", "home".equals(activeTab) ? "#2a4a6a" : "#1e2d3d");
        commandBuilder.set("#HomeTabLabel.Style.TextColor", "home".equals(activeTab) ? "#ffffff" : "#8899aa");
        commandBuilder.set("#HomeTabShortcut.Visible", "home".equals(activeTab));

        commandBuilder.set("#TutorielTab.Style.Default.Background", "tutoriel".equals(activeTab) ? "#2a4a6a" : "#1e2d3d");
        commandBuilder.set("#TutorielTabLabel.Style.TextColor", "tutoriel".equals(activeTab) ? "#ffffff" : "#8899aa");
        commandBuilder.set("#TutorielTabShortcut.Visible", "tutoriel".equals(activeTab));

        commandBuilder.set("#CommandesTab.Style.Default.Background", "commandes".equals(activeTab) ? "#2a4a6a" : "#1e2d3d");
        commandBuilder.set("#CommandesTabLabel.Style.TextColor", "commandes".equals(activeTab) ? "#ffffff" : "#8899aa");
        commandBuilder.set("#CommandesTabShortcut.Visible", "commandes".equals(activeTab));

        commandBuilder.set("#MisesAJourTab.Style.Default.Background", "misesajour".equals(activeTab) ? "#2a4a6a" : "#1e2d3d");
        commandBuilder.set("#MisesAJourTabLabel.Style.TextColor", "misesajour".equals(activeTab) ? "#ffffff" : "#8899aa");
        commandBuilder.set("#MisesAJourTabShortcut.Visible", "misesajour".equals(activeTab));

        commandBuilder.set("#VaryonTab.Style.Default.Background", "varyon".equals(activeTab) ? "#2a4a6a" : "#1e2d3d");
        commandBuilder.set("#VaryonTabLabel.Style.TextColor", "varyon".equals(activeTab) ? "#ffffff" : "#8899aa");
        commandBuilder.set("#VaryonTabShortcut.Visible", "varyon".equals(activeTab));

        commandBuilder.set("#InfosTab.Style.Default.Background", "infos".equals(activeTab) ? "#2a4a6a" : "#1e2d3d");
        commandBuilder.set("#InfosTabLabel.Style.TextColor", "infos".equals(activeTab) ? "#ffffff" : "#8899aa");
        commandBuilder.set("#InfosTabShortcut.Visible", "infos".equals(activeTab));

        commandBuilder.set("#AdminTabContainer.Visible", isAdmin);
        if (isAdmin) {
            commandBuilder.set("#AdminTab.Style.Default.Background", "admin".equals(activeTab) ? "#2a4a6a" : "#1e2d3d");
            commandBuilder.set("#AdminTabLabel.Style.TextColor", "admin".equals(activeTab) ? "#ffffff" : "#8899aa");
            commandBuilder.set("#AdminTabShortcut.Visible", "admin".equals(activeTab));
        }

        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#HomeTab", EventData.of("Action", "tab").append("Tab", "home"));
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#TutorielTab", EventData.of("Action", "tab").append("Tab", "tutoriel"));
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#CommandesTab", EventData.of("Action", "tab").append("Tab", "commandes"));
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#MisesAJourTab", EventData.of("Action", "tab").append("Tab", "misesajour"));
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#VaryonTab", EventData.of("Action", "tab").append("Tab", "varyon"));
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#InfosTab", EventData.of("Action", "tab").append("Tab", "infos"));
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#CloseButton", EventData.of("Action", "close"));

        if (isAdmin) {
            eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#AdminTab", EventData.of("Action", "tab").append("Tab", "admin"));
        }

        if ("commandes".equals(activeTab)) {
            buildCommandButtons(commandBuilder, eventBuilder);
        }
        if ("admin".equals(activeTab) && isAdmin) {
            buildAdminCommandButtons(commandBuilder, eventBuilder);
        }
    }

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
        commandBuilder.set("#InfosContent.Visible", "infos".equals(activeTab));
        commandBuilder.set("#AdminContent.Visible", "admin".equals(activeTab) && isAdmin);

        String tabName = switch (activeTab) {
            case "home" -> "ACCUEIL";
            case "tutoriel" -> "TUTORIEL";
            case "commandes" -> "COMMANDES";
            case "misesajour" -> "ACTUALIT\u00c9S";
            case "varyon" -> "VARYON";
            case "infos" -> "INFOS";
            case "admin" -> "ADMIN";
            default -> "ACCUEIL";
        };
        commandBuilder.set("#MenuTitle.TextSpans", Message.raw("VARYON - " + tabName));

        if ("commandes".equals(activeTab)) {
            buildCommandsContent(commandBuilder);
        }
        if ("admin".equals(activeTab) && isAdmin) {
            buildAdminCommandsContent(commandBuilder);
        }
        if ("misesajour".equals(activeTab)) {
            buildNewsContent(commandBuilder);
        }
        if ("home".equals(activeTab)) {
            buildHomeContent(commandBuilder);
        }
    }

    private void buildCommandsContent(@Nonnull UICommandBuilder commandBuilder) {
        List<CommandsConfig.CommandCategory> categories = CommandsConfig.getInstance().getCategories();

        for (int i = 1; i <= MAX_SLOTS; i++) {
            commandBuilder.set("#CategoryTitle" + i + ".Visible", false);
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
                    commandBuilder.set("#CategoryTitle" + slotIndex + ".Visible", true);
                    commandBuilder.set("#CategoryTitle" + slotIndex + ".TextSpans", Message.raw(category.getName()));
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
                }
            }
        }
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
                    "Group { LayoutMode: Left; Anchor: (Height: 54, Bottom: 4); }");
                listIdx++;

                int count = Math.min(BUTTONS_PER_ROW, buttons.size() - i);
                for (int j = 0; j < count; j++) {
                    CommandsConfig.CommandButton button = buttons.get(i + j);
                    String btnLabel = escapeForUI(button.getLabel());
                    String btnCmd = escapeForUI(button.getCommand());
                    boolean isLast = (j == count - 1);
                    String anchor = isLast ?
                        "Anchor: (Width: 162, Height: 50);" :
                        "Anchor: (Width: 162, Height: 50, Right: 2);";
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

    private void buildHomeContent(@Nonnull UICommandBuilder commandBuilder) {
        HomeConfig config = HomeConfig.getInstance();
        String content = config.getContent();
        
        commandBuilder.clear("#HomeTextContainer");
        
        String[] lines = content.split("\\R");
        boolean previousWasTitle = false;
        
        for (String line : lines) {
            line = line.trim();
            
            if (line.equals("[SEPARATOR]")) {
                commandBuilder.appendInline("#HomeTextContainer", "Group { Anchor: (Height: 8); }");
                commandBuilder.appendInline("#HomeTextContainer", "Group { Anchor: (Height: 1); Background: (Color: #4a5568); }");
                commandBuilder.appendInline("#HomeTextContainer", "Group { Anchor: (Height: 8); }");
                previousWasTitle = false;
            } else if (line.isEmpty()) {
                previousWasTitle = false;
            } else if (line.startsWith("[COLOR:") && line.contains("]")) {
                if (!previousWasTitle) {
                    commandBuilder.appendInline("#HomeTextContainer", "Group { Anchor: (Height: 8); }");
                }
                int colorEnd = line.indexOf("]");
                String colorCode = line.substring(7, colorEnd);
                String text = line.substring(colorEnd + 1).replace("[/COLOR]", "");
                text = escapeForUI(text);
                commandBuilder.appendInline("#HomeTextContainer", "Label { Text: \"" + text + "\"; Style: (FontSize: 16, TextColor: " + colorCode + ", RenderBold: true); }");
                commandBuilder.appendInline("#HomeTextContainer", "Group { Anchor: (Height: 4); }");
                previousWasTitle = true;
            } else {
                String text = escapeForUI(line);
                commandBuilder.appendInline("#HomeTextContainer", "Label { Text: \"" + text + "\"; Style: (FontSize: 14, TextColor: #dddddd); }");
                previousWasTitle = false;
            }
        }
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
                CommandManager.get().handleCommand(playerRefComponent, command);
            }
        }
    }

    public static class EventDataClass {
        public static final BuilderCodec<EventDataClass> CODEC =
            BuilderCodec.builder(EventDataClass.class, EventDataClass::new)
                .addField(new KeyedCodec<>("Action", Codec.STRING), (entry, s) -> entry.action = s, entry -> entry.action)
                .addField(new KeyedCodec<>("Tab", Codec.STRING), (entry, s) -> entry.tab = s, entry -> entry.tab)
                .addField(new KeyedCodec<>("Command", Codec.STRING), (entry, s) -> entry.command = s, entry -> entry.command)
                .build();

        public String action;
        public String tab;
        public String command;
    }
}
