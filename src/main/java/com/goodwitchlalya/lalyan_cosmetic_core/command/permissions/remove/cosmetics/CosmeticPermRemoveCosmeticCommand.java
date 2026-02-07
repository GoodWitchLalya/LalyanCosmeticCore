package com.goodwitchlalya.lalyan_cosmetic_core.command.permissions.remove.cosmetics;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.universe.Universe;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

/**
 *
 */
public class CosmeticPermRemoveCosmeticCommand extends CommandBase {
    
    private final Universe universe = Universe.get();
    
    /**
     *
     */
    public CosmeticPermRemoveCosmeticCommand() {
        super("cosmetic", "Remove a permission for a specific cosmetic");
        this.addSubCommand(new CosmeticPermRemoveCosmeticFromPlayerCommand());
        this.addSubCommand(new CosmeticPermRemoveCosmeticFromGroupCommand());
        this.setPermissionGroups("OP");
    }
    
    @Override
    protected void executeSync(@NonNullDecl CommandContext commandContext) {
    
    }
}