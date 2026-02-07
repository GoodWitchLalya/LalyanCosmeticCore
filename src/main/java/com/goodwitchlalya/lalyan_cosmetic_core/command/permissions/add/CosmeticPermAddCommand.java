package com.goodwitchlalya.lalyan_cosmetic_core.command.permissions.add;

import com.goodwitchlalya.lalyan_cosmetic_core.command.permissions.add.cosmetics.CosmeticPermAddCosmeticCommand;
import com.goodwitchlalya.lalyan_cosmetic_core.command.permissions.add.slots.CosmeticPermAddSlotCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.universe.Universe;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

/**
 *
 */
public class CosmeticPermAddCommand extends CommandBase {
    
    /**
     *
     */
    public CosmeticPermAddCommand() {
        super("add", "Add a permission");
        this.addSubCommand(new CosmeticPermAddCosmeticCommand());
        this.addSubCommand(new CosmeticPermAddSlotCommand());
        this.setPermissionGroups("OP");
    }
    
    @Override
    protected void executeSync(@NonNullDecl CommandContext commandContext) {
    
    }
}