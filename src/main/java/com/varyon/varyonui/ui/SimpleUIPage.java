package com.varyon.varyonui.ui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

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
        commandBuilder.append("Pages/VaryonMainPage.ui");
        
        buildTabBar(commandBuilder, eventBuilder);
        buildContent(commandBuilder, eventBuilder, store, ref);
    }

    private void buildTabBar(@Nonnull UICommandBuilder commandBuilder,
                             @Nonnull UIEventBuilder eventBuilder) {
        commandBuilder.set("#HomeTab.Disabled", "home".equals(activeTab));
        commandBuilder.set("#TutorielTab.Disabled", "tutoriel".equals(activeTab));
        commandBuilder.set("#CommandesTab.Disabled", "commandes".equals(activeTab));
        commandBuilder.set("#MisesAJourTab.Disabled", "misesajour".equals(activeTab));
        commandBuilder.set("#InfosTab.Disabled", "infos".equals(activeTab));
        
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
            case "misesajour" -> "ACTUALITES";
            case "infos" -> "INFOS";
            default -> "ACCUEIL";
        };
        commandBuilder.set("#MenuTitle.TextSpans", Message.raw("VARYON - " + tabName));
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
        }
    }

    public static class EventDataClass {
        public static final BuilderCodec<EventDataClass> CODEC =
            BuilderCodec.builder(EventDataClass.class, EventDataClass::new)
                .addField(new KeyedCodec<>("Action", Codec.STRING), (entry, s) -> entry.action = s, entry -> entry.action)
                .addField(new KeyedCodec<>("Tab", Codec.STRING), (entry, s) -> entry.tab = s, entry -> entry.tab)
                .build();
        
        public String action;
        public String tab;
    }
}
