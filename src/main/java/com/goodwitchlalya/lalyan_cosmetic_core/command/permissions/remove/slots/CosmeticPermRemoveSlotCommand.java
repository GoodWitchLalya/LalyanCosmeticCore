package com.goodwitchlalya.lalyan_cosmetic_core.command.permissions.remove.slots;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.universe.Universe;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

/**
 *
 */
public class CosmeticPermRemoveSlotCommand extends CommandBase {
    
    /**
     *
     */
    public CosmeticPermRemoveSlotCommand() {
        super("slot", "Remove a permission for a specific cosmetic");
        this.addSubCommand(new CosmeticPermRemoveSlotFromPlayerCommand());
        this.addSubCommand(new CosmeticPermRemoveSlotFromGroupCommand());
        this.setPermissionGroups("OP");
    }
    
    @Override
    protected void executeSync(@NonNullDecl CommandContext commandContext) {
    
    }
}