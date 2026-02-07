package com.goodwitchlalya.lalyan_cosmetic_core.command.permissions.check.slots;

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
public class CosmeticPermCheckSlotFromPlayerCommand extends CommandBase {
    
    private final Universe universe = Universe.get();
    
    private RequiredArg<String> slotID;
    private RequiredArg<PlayerRef> player;
    
    /**
     *
     */
    public CosmeticPermCheckSlotFromPlayerCommand() {
        super("player", "Check the permission for a specific cosmetic from a player by player name");
        this.slotID = this.withRequiredArg("Slot ID", "The slot's name", ArgTypes.STRING);
        this.player = this.withRequiredArg("Player reference", "The player's name", ArgTypes.PLAYER_REF);
        this.setPermissionGroups("OP");
    }
    
    @Override
    protected void executeSync(@NonNullDecl CommandContext commandContext) {
        UUID uuid = player.get(commandContext).getUuid();
        
        Set<String> permission = CosmeticCore.slotIdToPermissionUse(slotID.get(commandContext));
        String permissionString = CosmeticCore.slotIdToPermissionStringUse(slotID.get(commandContext));
        
        if (PermissionsModule.get().hasPermission(uuid, permissionString)) {
            commandContext.sendMessage(Message.raw(String.format("%s has the permission (%s) for %s", player.get(commandContext).getUsername(), permissionString, slotID.get(commandContext))));
        } else {
            commandContext.sendMessage(Message.raw(String.format("%s hasn't the permission (%s) for %s", player.get(commandContext).getUsername(), permissionString, slotID.get(commandContext))));
        }
    }
}