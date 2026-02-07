package com.goodwitchlalya.lalyan_cosmetic_core.command.permissions.check.cosmetics;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.universe.Universe;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

/**
 *
 */
public class CosmeticPermCheckCosmeticCommand extends CommandBase {
    
    /**
     *
     */
    public CosmeticPermCheckCosmeticCommand() {
        super("cosmetic", "Check a permission for a specific cosmetic");
        this.addSubCommand(new CosmeticPermCheckCosmeticFromPlayerCommand());
        this.setPermissionGroups("OP");
    }
    
    @Override
    protected void executeSync(@NonNullDecl CommandContext commandContext) {
    
    }
}