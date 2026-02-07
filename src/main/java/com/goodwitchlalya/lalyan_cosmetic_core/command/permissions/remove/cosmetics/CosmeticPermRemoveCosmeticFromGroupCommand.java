package com.goodwitchlalya.lalyan_cosmetic_core.command.permissions.remove.cosmetics;

import com.goodwitchlalya.lalyan_cosmetic_core.CosmeticCore;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.universe.Universe;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

/**
 *
 */
public class CosmeticPermRemoveCosmeticFromGroupCommand extends CommandBase {
    
    private final Universe universe = Universe.get();
    
    private final RequiredArg<String> cosmeticID;
    private final RequiredArg<String> group;
    
    /**
     *
     */
    public CosmeticPermRemoveCosmeticFromGroupCommand() {
        super("group", "Manage the cosmetics' permissions");
        this.cosmeticID = this.withRequiredArg("permission name", "The cosmetic ID", ArgTypes.STRING);
        this.group = this.withRequiredArg("group name", "The permission name", ArgTypes.STRING);
        this.setPermissionGroups("OP");
    }
    
    @Override
    protected void executeSync(@NonNullDecl CommandContext commandContext) {
        PermissionsModule.get().removeGroupPermission(group.get(commandContext), CosmeticCore.cosmeticIdToPermissionUse(cosmeticID.get(commandContext)));
        
        commandContext.sendMessage(Message.raw(String.format("Removed the permission (%s) for %s (Group) to %s", CosmeticCore.cosmeticIdToPermissionUse(cosmeticID.get(commandContext)), group.get(commandContext), cosmeticID.get(commandContext))));
    }
}