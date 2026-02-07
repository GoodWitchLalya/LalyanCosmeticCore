package com.goodwitchlalya.lalyan_cosmetic_core.command.permissions.check;

import com.goodwitchlalya.lalyan_cosmetic_core.command.permissions.check.cosmetics.CosmeticPermCheckCosmeticCommand;
import com.goodwitchlalya.lalyan_cosmetic_core.command.permissions.check.cosmetics.CosmeticPermCheckCosmeticFromPlayerCommand;
import com.goodwitchlalya.lalyan_cosmetic_core.command.permissions.check.slots.CosmeticPermCheckSlotCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.universe.Universe;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

/**
 *
 */
public class CosmeticPermCheckCommand extends CommandBase {
    
    /**
     *
     */
    public CosmeticPermCheckCommand() {
        super("check", "Check a permission for a specific cosmetic");
        this.addSubCommand(new CosmeticPermCheckCosmeticCommand());
        this.addSubCommand(new CosmeticPermCheckSlotCommand());
        this.setPermissionGroups("OP");
    }
    
    @Override
    protected void executeSync(@NonNullDecl CommandContext commandContext) {
    
    }
}