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

/**
 * Represents a custom interaction that opens the cosmetic customization GUI for a player.
 * This interaction is triggered when a player interacts with a specific entity or block
 * configured to use this interaction type.
 */
public class OpenCosmeticPageInteraction extends SimpleInstantInteraction {
    
    /**
     * Codec for serialization and deserialization of this interaction.
     * This is necessary for the server to recognize and handle this custom interaction.
     */
    public static final BuilderCodec<OpenCosmeticPageInteraction> CODEC = BuilderCodec.builder(OpenCosmeticPageInteraction.class, OpenCosmeticPageInteraction::new)
        .build();
    
    /**
     * This method is executed when the player performs the interaction for the first time (or after the cooldown has expired).
     * It opens the cosmetic customization page for the interacting player.
     *
     * @param interactionType The type of interaction performed (e.g., left-click, right-click).
     * @param ctx The context of the interaction, containing information about the interacting entity.
     * @param cooldownHandler A handler to manage cooldowns for this interaction.
     */
    @Override
    protected void firstRun(@NonNullDecl InteractionType interactionType, @NonNullDecl InteractionContext ctx, @NonNullDecl CooldownHandler cooldownHandler) {
        // Get the entity reference and the entity store from the interaction context.
        Ref<EntityStore> ref = ctx.getEntity();
        Store<EntityStore> store = ref.getStore();
        CommandBuffer<EntityStore> commandBuffer = ctx.getCommandBuffer();
        
        // Get the Player component from the interacting entity.
        Player player = commandBuffer.getComponent(ref, Player.getComponentType());
        
        // If the interacting entity is not a player, do nothing.
        if (player == null) {
            return;
        }
        
        // Open the custom cosmetic page for the player.
        // It retrieves the player's unique reference from the Universe to ensure the GUI is opened for the correct player.
        player.getPageManager().openCustomPage(ref, store, new CosmeticPage(Universe.get().getPlayer(player.getDisplayName(), NameMatching.EXACT)));
    }
}
