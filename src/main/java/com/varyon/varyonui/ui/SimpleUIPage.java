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
import com.varyon.varyonui.config.CommandsConfig;

import javax.annotation.Nonnull;
import java.util.List;

public class SimpleUIPage extends InteractiveCustomUIPage<SimpleUIPage.EventDataClass> {

    private String activeTab = "home";

    public SimpleUIPage(@Nonnull PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss, EventDataClass.CODEC);
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
        commandBuilder.set("#HomeTabBg.Background", "home".equals(activeTab) ? "(Color: #2a4a6a)" : "(Color: #1e2d3d)");
        commandBuilder.set("#TutorielTabBg.Background", "tutoriel".equals(activeTab) ? "(Color: #2a4a6a)" : "(Color: #1e2d3d)");
        commandBuilder.set("#CommandesTabBg.Background", "commandes".equals(activeTab) ? "(Color: #2a4a6a)" : "(Color: #1e2d3d)");
        commandBuilder.set("#MisesAJourTabBg.Background", "misesajour".equals(activeTab) ? "(Color: #2a4a6a)" : "(Color: #1e2d3d)");
        commandBuilder.set("#InfosTabBg.Background", "infos".equals(activeTab) ? "(Color: #2a4a6a)" : "(Color: #1e2d3d)");
        
        eventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#HomeTab",
            EventData.of("Action", "tab").append("Tab", "home")
        );
        eventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#TutorielTab",
            EventData.of("Action", "tab").append("Tab", "tutoriel")
        );
        eventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#CommandesTab",
            EventData.of("Action", "tab").append("Tab", "commandes")
        );
        eventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#MisesAJourTab",
            EventData.of("Action", "tab").append("Tab", "misesajour")
        );
        eventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#InfosTab",
            EventData.of("Action", "tab").append("Tab", "infos")
        );
        eventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#CloseButton",
            EventData.of("Action", "close")
        );
        
        if ("commandes".equals(activeTab)) {
            buildCommandButtons(commandBuilder, eventBuilder);
        }
    }

    private void buildCommandButtons(@Nonnull UICommandBuilder commandBuilder,
                                     @Nonnull UIEventBuilder eventBuilder) {
        List<CommandsConfig.CommandCategory> categories = CommandsConfig.getInstance().getCategories();
        
        int buttonIndex = 0;
        for (CommandsConfig.CommandCategory category : categories) {
            for (CommandsConfig.CommandButton button : category.getButtons()) {
                buttonIndex++;
                String buttonId = "#CommandButton" + buttonIndex;
                
                String action = button.getType() == CommandsConfig.CommandType.CHAT ? "chatcommand" : "command";
                
                eventBuilder.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    buttonId,
                    EventData.of("Action", action).append("Command", button.getCommand())
                );
            }
        }
    }

    private void buildContent(@Nonnull UICommandBuilder commandBuilder,
                              @Nonnull UIEventBuilder eventBuilder,
                              @Nonnull Store<EntityStore> store,
                              @Nonnull Ref<EntityStore> ref) {
        commandBuilder.set("#HomeContent.Visible", "home".equals(activeTab));
        commandBuilder.set("#TutorielContent.Visible", "tutoriel".equals(activeTab));
        commandBuilder.set("#CommandesContent.Visible", "commandes".equals(activeTab));
        commandBuilder.set("#MisesAJourContent.Visible", "misesajour".equals(activeTab));
        commandBuilder.set("#InfosContent.Visible", "infos".equals(activeTab));
        
        String tabName = switch (activeTab) {
            case "home" -> "ACCUEIL";
            case "tutoriel" -> "TUTORIEL";
            case "commandes" -> "COMMANDES";
            case "misesajour" -> "MISES À JOUR";
            case "infos" -> "INFOS";
            default -> "ACCUEIL";
        };
        commandBuilder.set("#MenuTitle.TextSpans", Message.raw("VARYON - " + tabName));
        
        if ("commandes".equals(activeTab)) {
            buildCommandsContent(commandBuilder);
        }
    }

    private void buildCommandsContent(@Nonnull UICommandBuilder commandBuilder) {
        List<CommandsConfig.CommandCategory> categories = CommandsConfig.getInstance().getCategories();
        
        // Masquer tous les boutons et titres par défaut
        for (int i = 1; i <= 8; i++) {
            commandBuilder.set("#CommandButton" + i + ".Visible", false);
        }
        commandBuilder.set("#CategoryTitle1.Visible", false);
        commandBuilder.set("#CategoryTitle2.Visible", false);
        
        int buttonIndex = 0;
        int titleIndex = 1;
        
        for (CommandsConfig.CommandCategory category : categories) {
            if (titleIndex > 2) break; // Max 2 catégories
            
            // Afficher et configurer le titre
            commandBuilder.set("#CategoryTitle" + titleIndex + ".Visible", true);
            commandBuilder.set("#CategoryTitle" + titleIndex + ".TextSpans", Message.raw(category.getName()));
            titleIndex++;
            
            // Afficher et configurer les boutons
            for (CommandsConfig.CommandButton button : category.getButtons()) {
                buttonIndex++;
                if (buttonIndex > 8) break; // Max 8 boutons
                
                commandBuilder.set("#CommandButton" + buttonIndex + ".Visible", true);
                commandBuilder.set("#CommandButton" + buttonIndex + "Label.TextSpans", Message.raw(button.getLabel()));
                commandBuilder.set("#CommandButton" + buttonIndex + "Cmd.TextSpans", Message.raw(button.getCommand()));
            }
        }
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref,
                                 @Nonnull Store<EntityStore> store,
                                 @Nonnull EventDataClass data) {
        if ("tab".equals(data.action) && data.tab != null) {
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
                
                playerRefComponent.getPacketHandler().write((Packet) new OpenChatWithCommand(data.command));
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
