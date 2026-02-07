package com.goodwitchlalya.lalyan_cosmetic_core.command.permissions.remove;

import com.goodwitchlalya.lalyan_cosmetic_core.command.permissions.remove.cosmetics.CosmeticPermRemoveCosmeticCommand;
import com.goodwitchlalya.lalyan_cosmetic_core.command.permissions.remove.cosmetics.CosmeticPermRemoveCosmeticFromGroupCommand;
import com.goodwitchlalya.lalyan_cosmetic_core.command.permissions.remove.cosmetics.CosmeticPermRemoveCosmeticFromPlayerCommand;
import com.goodwitchlalya.lalyan_cosmetic_core.command.permissions.remove.slots.CosmeticPermRemoveSlotCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.universe.Universe;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

/**
 *
 */
public class CosmeticPermRemoveCommand extends CommandBase {
    
    private final Universe universe = Universe.get();
    
    /**
     *
     */
    public CosmeticPermRemoveCommand() {
        super("remove", "Remove a permission for a specific cosmetic");
        this.addSubCommand(new CosmeticPermRemoveCosmeticCommand());
        this.addSubCommand(new CosmeticPermRemoveSlotCommand());
        this.setPermissionGroups("OP");
    }
    
    @Override
    protected void executeSync(@NonNullDecl CommandContext commandContext) {
    
    }
}