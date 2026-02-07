package com.goodwitchlalya.lalyan_cosmetic_core.command.permissions.add.cosmetics;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.universe.Universe;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

/**
 *
 */
public class CosmeticPermAddCosmeticCommand extends CommandBase {
    
    /**
     *
     */
    public CosmeticPermAddCosmeticCommand() {
        super("cosmetic", "Add a cosmetic's permission");
        this.addSubCommand(new CosmeticPermAddCosmeticToPlayerCommand());
        this.addSubCommand(new CosmeticPermAddCosmeticToGroupCommand());
        this.setPermissionGroups("OP");
    }
    
    @Override
    protected void executeSync(@NonNullDecl CommandContext commandContext) {
    
    }
}