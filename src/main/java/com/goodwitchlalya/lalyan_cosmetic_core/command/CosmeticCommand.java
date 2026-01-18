package com.goodwitchlalya.lalyan_cosmetic_core.command;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;


public class CosmeticCommand extends AbstractPlayerCommand {
    public CosmeticCommand() {
        super("cosmetic", "Does nothing. Use the subcommands!");
        this.addSubCommand(new CosmeticApplyCommand());
        this.addSubCommand(new CosmeticClearCommand());
        this.addSubCommand(new CosmeticReloadCommand());
        this.addSubCommand(new CosmeticListCommand());
        this.addSubCommand(new CosmeticChangeCommand());
        this.setPermissionGroup(GameMode.Adventure);
    }
    
    @Override
    protected void execute(@NonNullDecl CommandContext commandContext, @NonNullDecl Store<EntityStore> store, @NonNullDecl Ref<EntityStore> ref, @NonNullDecl PlayerRef playerRef, @NonNullDecl World world) {
    }
    
}