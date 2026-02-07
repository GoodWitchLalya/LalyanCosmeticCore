package com.goodwitchlalya.lalyan_cosmetic_core.command.permissions;

import com.goodwitchlalya.lalyan_cosmetic_core.CosmeticCore;
import com.goodwitchlalya.lalyan_cosmetic_core.command.permissions.add.CosmeticPermAddCommand;
import com.goodwitchlalya.lalyan_cosmetic_core.command.permissions.add.cosmetics.CosmeticPermAddCosmeticCommand;
import com.goodwitchlalya.lalyan_cosmetic_core.command.permissions.check.CosmeticPermCheckCommand;
import com.goodwitchlalya.lalyan_cosmetic_core.command.permissions.check.cosmetics.CosmeticPermCheckCosmeticCommand;
import com.goodwitchlalya.lalyan_cosmetic_core.command.permissions.remove.CosmeticPermRemoveCommand;
import com.goodwitchlalya.lalyan_cosmetic_core.command.permissions.remove.cosmetics.CosmeticPermRemoveCosmeticCommand;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import com.hypixel.hytale.server.core.universe.Universe;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import static com.goodwitchlalya.lalyan_cosmetic_core.CosmeticCore.log;

/**
 *
 */
public class CosmeticPermsCommand extends CommandBase {
    
    private final Universe universe = Universe.get();
    
    /**
     *
     */
    public CosmeticPermsCommand() {
        super("perms", "Manage the cosmetics' permissions");
        this.setPermissionGroups("OP");
        this.addSubCommand(new CosmeticPermAddCommand());
        this.addSubCommand(new CosmeticPermRemoveCommand());
        this.addSubCommand(new CosmeticPermCheckCommand());
    }
    
    @Override
    protected void executeSync(@NonNullDecl CommandContext commandContext) {
    
    }
}