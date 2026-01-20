package com.goodwitchlalya.lalyan_cosmetic_core.command;

import com.goodwitchlalya.lalyan_cosmetic_core.util.AttachmentsRegistry;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

/**
 * Subcommand to remove all applied custom cosmetics from a player.
 * Requires OP permissions.
 */
public class CosmeticClearCommand extends AbstractPlayerCommand {
    
    private final Universe universe = Universe.get();
    
    /**
     * Constructor for the 'clear' subcommand.
     * Defines the command's description and permissions.
     */
    public CosmeticClearCommand() {
        super("clear", "Removes all custom cosmetics applied");
        this.setPermissionGroups("OP");
    }
    
    /**
     * Executes the command logic.
     * Clears all cosmetics from the player who executed the command.
     */
    @Override
    protected void execute(@NonNullDecl CommandContext commandContext, @NonNullDecl Store<EntityStore> store, @NonNullDecl Ref<EntityStore> ref, @NonNullDecl PlayerRef playerRef, @NonNullDecl World world) {
        // Execute the clearing logic on the world's main thread.
        universe.getWorld(playerRef.getWorldUuid()).execute(() -> {
            AttachmentsRegistry.get().clearAll(ref);
        });
    }
    
}