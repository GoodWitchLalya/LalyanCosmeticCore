package com.goodwitchlalya.lalyan_cosmetic_core.command.permissions.remove.slots;

import com.goodwitchlalya.lalyan_cosmetic_core.CosmeticCore;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.universe.Universe;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.Set;

/**
 *
 */
public class CosmeticPermRemoveSlotFromGroupCommand extends CommandBase {
    
    private RequiredArg<String> slotID;
    private RequiredArg<String> group;
    
    /**
     *
     */
    public CosmeticPermRemoveSlotFromGroupCommand() {
        super("group", "Manage the cosmetics' permissions");
        this.slotID = this.withRequiredArg("Slot ID", "The slot's ID", ArgTypes.STRING);
        this.group = this.withRequiredArg("group name", "The group's name", ArgTypes.STRING);
        this.setPermissionGroups("OP");
    }
    
    @Override
    protected void executeSync(@NonNullDecl CommandContext commandContext) {
        Set<String> permission = CosmeticCore.slotIdToPermissionUse(slotID.get(commandContext));
        String permissionString = CosmeticCore.slotIdToPermissionStringUse(slotID.get(commandContext));
        
        CosmeticCore.removePerm(group.get(commandContext), permission);
        
        commandContext.sendMessage(Message.raw(String.format("Removed the permission (%s) for %s (Group) to %s", permissionString, group.get(commandContext), slotID.get(commandContext))));
    }
}