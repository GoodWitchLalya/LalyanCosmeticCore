package com.goodwitchlalya.lalyan_cosmetic_core.command;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

/**
 * This is an example command that will simply print the name of the plugin in chat when used.
 */
public class CosmeticCommand extends AbstractPlayerCommand {
    
    private final String pluginName;
    private final String pluginVersion;
    private final Universe universe = Universe.get();
    private RequiredArg<String> action;
    
    public CosmeticCommand(String pluginName, String pluginVersion) {
        super("cosmetic", "Prints a test message from the " + pluginName + " plugin.");
        this.action = this.withRequiredArg("action", "", ArgTypes.STRING);
        this.addSubCommand(new CosmeticApplyCommand());
        this.addSubCommand(new CosmeticResetCommand());
        this.addSubCommand(new CosmeticReloadCommand());
        this.addSubCommand(new CosmeticListCommand());
        this.setPermissionGroup(GameMode.Adventure); // Allows the command to be used by anyone, not just OP
        this.pluginName = pluginName;
        this.pluginVersion = pluginVersion;
    }
    
    @Override
    protected void execute(@NonNullDecl CommandContext commandContext, @NonNullDecl Store<EntityStore> store, @NonNullDecl Ref<EntityStore> ref, @NonNullDecl PlayerRef playerRef, @NonNullDecl World world) {
    }
    
}