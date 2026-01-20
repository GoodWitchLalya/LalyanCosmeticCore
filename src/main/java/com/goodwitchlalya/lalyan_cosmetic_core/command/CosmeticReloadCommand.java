package com.goodwitchlalya.lalyan_cosmetic_core.command;

import com.goodwitchlalya.lalyan_cosmetic_core.util.FileManager;
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
 * Subcommand to manually trigger a reload of all cosmetic assets.
 * Requires OP permissions.
 */
public class CosmeticReloadCommand extends AbstractPlayerCommand {
    
    private final Universe universe = Universe.get();
    
    /**
     * Constructor for the 'reload' subcommand.
     * Defines the command's description and permissions.
     */
    public CosmeticReloadCommand() {
        super("reload", "Manually reloads cosmetics");
        this.setPermissionGroups("OP");
    }
    
    /**
     * Executes the command logic.
     * Triggers the asset reloading process and sends the log output to the player.
     */
    @Override
    protected void execute(@NonNullDecl CommandContext commandContext, @NonNullDecl Store<EntityStore> store, @NonNullDecl Ref<EntityStore> ref, @NonNullDecl PlayerRef playerRef, @NonNullDecl World world) {
        // Execute the reload on the world's main thread.
        universe.getWorld(playerRef.getWorldUuid()).execute(() -> {
            // Call the FileManager to reload assets and send the resulting log to the command sender.
            commandContext.sendMessage(Message.raw(FileManager.wakeUp()));
        });
    }
    
}