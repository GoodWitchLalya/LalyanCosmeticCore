package com.goodwitchlalya.lalyan_cosmetic_core.command.permissions.check.slots;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.universe.Universe;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

/**
 *
 */
public class CosmeticPermCheckSlotCommand extends CommandBase {
    
    private final Universe universe = Universe.get();
    
    /**
     *
     */
    public CosmeticPermCheckSlotCommand() {
        super("slot", "Check a permission for a specific cosmetic");
        this.addSubCommand(new CosmeticPermCheckSlotFromPlayerCommand());
        this.setPermissionGroups("OP");
    }
    
    @Override
    protected void executeSync(@NonNullDecl CommandContext commandContext) {
    
    }
}