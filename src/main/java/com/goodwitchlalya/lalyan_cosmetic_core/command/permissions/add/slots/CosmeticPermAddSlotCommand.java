package com.goodwitchlalya.lalyan_cosmetic_core.command.permissions.add.slots;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.universe.Universe;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

/**
 *
 */
public class CosmeticPermAddSlotCommand extends CommandBase {
    
    private final Universe universe = Universe.get();
    
    /**
     *
     */
    public CosmeticPermAddSlotCommand() {
        super("slot", "Add a cosmetic's permission");
        this.addSubCommand(new CosmeticPermAddSlotToPlayerCommand());
        this.addSubCommand(new CosmeticPermAddSlotToGroupCommand());
        this.setPermissionGroups("OP");
    }
    
    @Override
    protected void executeSync(@NonNullDecl CommandContext commandContext) {
    
    }
}