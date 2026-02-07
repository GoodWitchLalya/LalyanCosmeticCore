package com.goodwitchlalya.lalyan_cosmetic_core.command.permissions.add.slots;

import com.goodwitchlalya.lalyan_cosmetic_core.CosmeticCore;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.Set;
import java.util.UUID;

/**
 *
 */
public class CosmeticPermAddSlotToPlayerCommand extends CommandBase {
    
    private RequiredArg<String> slotID;
    private RequiredArg<PlayerRef> player;
    
    /**
     *
     */
    public CosmeticPermAddSlotToPlayerCommand() {
        super("player", "");
        this.slotID = this.withRequiredArg("Slot ID", "The permission name", ArgTypes.STRING);
        this.player = this.withRequiredArg("Player reference", "The permission name", ArgTypes.PLAYER_REF);
        this.setPermissionGroups("OP");
    }
    
    @Override
    protected void executeSync(@NonNullDecl CommandContext commandContext) {
        
        UUID uuid = player.get(commandContext).getUuid();
        
        Set<String> permission = CosmeticCore.slotIdToPermissionUse(slotID.get(commandContext));
        String permissionString = CosmeticCore.slotIdToPermissionStringUse(slotID.get(commandContext));
        
        if (CosmeticCore.getPerm(player.get(commandContext), permission)) {
            commandContext.sendMessage(Message.raw(String.format("%s already have the permission (%s) for %s", player.get(commandContext).getUsername(), permissionString, slotID.get(commandContext))));
        } else {
            CosmeticCore.addPerm(player.get(commandContext), permission);
            
            if (CosmeticCore.getPerm(player.get(commandContext), permission)) {
                commandContext.sendMessage(Message.raw(String.format("Added the permission (%s) for %s to %s", permissionString, player.get(commandContext).getUsername(), slotID.get(commandContext))));
            } else {
                commandContext.sendMessage(Message.raw(String.format("Failed to add the permission (%s) for %s to %s", permissionString, player.get(commandContext).getUsername(), slotID.get(commandContext))));
            }
        }
    }
}