package com.goodwitchlalya.lalyan_cosmetic_core.command.permissions.remove.cosmetics;

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
public class CosmeticPermRemoveCosmeticFromPlayerCommand extends CommandBase {
    
    private RequiredArg<String> cosmeticID;
    private RequiredArg<PlayerRef> player;
    
    /**
     *
     */
    public CosmeticPermRemoveCosmeticFromPlayerCommand() {
        super("player", "Remove the permission for a specific cosmetic from a player by player name");
        this.cosmeticID = this.withRequiredArg("Permission name", "The permission name", ArgTypes.STRING);
        this.player = this.withRequiredArg("Player reference", "The permission name", ArgTypes.PLAYER_REF);
        this.setPermissionGroups("OP");
    }
    
    @Override
    protected void executeSync(@NonNullDecl CommandContext commandContext) {
        
        
        UUID uuid = player.get(commandContext).getUuid();
        
        Set<String> permission = CosmeticCore.cosmeticIdToPermissionUse(cosmeticID.get(commandContext));
        String permissionString = CosmeticCore.cosmeticIdToPermissionStringUse(cosmeticID.get(commandContext));
        
        if (!CosmeticCore.getPerm(player.get(commandContext), permission)) {
            commandContext.sendMessage(Message.raw(String.format("%s doesn't have the permission (%s) for %s", player.get(commandContext).getUsername(), permissionString, cosmeticID.get(commandContext))));
        } else {
            CosmeticCore.removePerm(player.get(commandContext), permission);
            
            if (!CosmeticCore.getPerm(player.get(commandContext), permission)) {
                commandContext.sendMessage(Message.raw(String.format("Removed the permission (%s) for %s to %s", permissionString, player.get(commandContext).getUsername(), cosmeticID.get(commandContext))));
            } else {
                commandContext.sendMessage(Message.raw(String.format("Failed to remove the permission (%s) for %s to %s", permissionString, player.get(commandContext).getUsername(), cosmeticID.get(commandContext))));
            }
        }
    }
}