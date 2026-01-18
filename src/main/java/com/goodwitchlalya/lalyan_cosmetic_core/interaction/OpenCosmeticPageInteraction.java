package com.goodwitchlalya.lalyan_cosmetic_core.interaction;

import com.goodwitchlalya.lalyan_cosmetic_core.gui.page.CosmeticPage;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class OpenCosmeticPageInteraction extends SimpleInstantInteraction {
    
    public static final BuilderCodec<OpenCosmeticPageInteraction> CODEC = BuilderCodec.builder(OpenCosmeticPageInteraction.class, OpenCosmeticPageInteraction::new)
        .build();
    
    @Override
    protected void firstRun(@NonNullDecl InteractionType interactionType, @NonNullDecl InteractionContext ctx, @NonNullDecl CooldownHandler cooldownHandler) {
        Ref<EntityStore> ref = ctx.getEntity();
        Store<EntityStore> store = ref.getStore();
        CommandBuffer<EntityStore> commandBuffer = ctx.getCommandBuffer();
        
        Player player = commandBuffer.getComponent(ref, Player.getComponentType());
        
        if (player == null) {
            return;
        }
        
        player.getPageManager().openCustomPage(ref, store, new CosmeticPage(Universe.get().getPlayer(player.getDisplayName(), NameMatching.EXACT)));
    }
}
