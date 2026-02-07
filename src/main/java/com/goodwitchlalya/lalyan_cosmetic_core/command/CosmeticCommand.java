package com.goodwitchlalya.lalyan_cosmetic_core.command;

import com.goodwitchlalya.lalyan_cosmetic_core.command.permissions.CosmeticPermsCommand;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;


/**
 * Main command for the cosmetic system.
 * This command acts as a container for all cosmetic-related subcommands.
 * It doesn't perform any action by itself.
 */
public class CosmeticCommand extends AbstractPlayerCommand {
    /**
     * Constructor for the main cosmetic command.
     * It registers all the necessary subcommands.
     */
    public CosmeticCommand() {
        super("cosmetic", "Does nothing. Use the subcommands!");
        this.addSubCommand(new CosmeticApplyCommand());
        this.addSubCommand(new CosmeticClearCommand());
        this.addSubCommand(new CosmeticReloadCommand());
        this.addSubCommand(new CosmeticListCommand());
        this.addSubCommand(new CosmeticChangeCommand());
        this.addSubCommand(new CosmeticReskinCommand());
        this.addSubCommand(new CosmeticPermsCommand());
        this.setPermissionGroup(GameMode.Adventure);
    }
    
    /**
     * The execution logic for the base command.
     * This is intentionally left empty as the functionality is handled by the subcommands.
     */
    @Override
    protected void execute(@NonNullDecl CommandContext commandContext, @NonNullDecl Store<EntityStore> store, @NonNullDecl Ref<EntityStore> ref, @NonNullDecl PlayerRef playerRef, @NonNullDecl World world) {
        // The base command does nothing on its own.
    }
    
}