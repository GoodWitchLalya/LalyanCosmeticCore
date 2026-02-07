package com.goodwitchlalya.lalyan_cosmetic_core.command;

import com.goodwitchlalya.lalyan_cosmetic_core.util.AttachmentsRegistry;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

/**
 * Subcommand to list all registered and loaded cosmetics.
 * Requires OP permissions.
 */
public class CosmeticListCommand extends AbstractPlayerCommand {
    
    /**
     * Constructor for the 'list' subcommand.
     * Defines the command's description and permissions.
     */
    public CosmeticListCommand() {
        super("list", "Lists all Cosmetics found and loaded");
        this.setPermissionGroups("OP");
    }
    
    /**
     * Executes the command logic.
     * Sends a list of all registered cosmetic IDs to the player who executed the command.
     */
    @Override
    protected void execute(@NonNullDecl CommandContext commandContext, @NonNullDecl Store<EntityStore> store, @NonNullDecl Ref<EntityStore> ref, @NonNullDecl PlayerRef playerRef, @NonNullDecl World world) {
        // Execute the listing on the world's main thread.
        Universe.get().getWorld(playerRef.getWorldUuid()).execute(() -> {
            commandContext.sendMessage(Message.raw("Cosmetics:"));
            // Retrieve the list from the registry and send each entry as a message.
            AttachmentsRegistry.get().getAttachmentsList().forEach(attachment -> {
                commandContext.sendMessage(Message.raw(String.format("- %s", attachment)));
            });
        });
    }
    
}