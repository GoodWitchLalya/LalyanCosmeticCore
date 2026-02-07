package com.goodwitchlalya.lalyan_cosmetic_core.util;

import com.goodwitchlalya.lalyan_cosmetic_core.CosmeticCore;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.platform.PlayerAdapter;

public class LuckpermsCompatibility {

    private static LuckpermsCompatibility instance;
    
    private static LuckPerms luckPerms;
    private static PlayerAdapter<PlayerRef> playerAdapter;

    private LuckpermsCompatibility() {
        luckPerms = LuckPermsProvider.get();
        playerAdapter = LuckPermsProvider.get().getPlayerAdapter(PlayerRef.class);
    }

    public static LuckpermsCompatibility getInstance() {
        if (instance == null) {
            instance = new LuckpermsCompatibility();
        }
        return instance;
    }
    
    public void addPerm(PlayerRef playerRef, String permission) {
        
        User user = playerAdapter.getUser(playerRef);
        //CachedPermissionData permissionData = playerAdapter.getPermissionData(playerRef);
        //CachedMetaData metaData = playerAdapter.getMetaData(playerRef);
        
        user.data().add(Node.builder(permission).build());
        luckPerms.getUserManager().saveUser(user);
        
    }
    
    public void addPerm(String groupName, String permission) {
        
        Group group = luckPerms.getGroupManager().getGroup(groupName);
        //CachedMetaData metaData = playerAdapter.getMetaData(playerRef);
        
        assert group != null;
        group.data().add(Node.builder(permission).build());
        luckPerms.getGroupManager().saveGroup(group);
        
    }
    
    public void removePerm(PlayerRef playerRef, String permission) {
        
        User user = playerAdapter.getUser(playerRef);
        //CachedPermissionData permissionData = playerAdapter.getPermissionData(playerRef);
        //CachedMetaData metaData = playerAdapter.getMetaData(playerRef);
        
        user.data().remove(Node.builder(permission).build());
        luckPerms.getUserManager().saveUser(user);
        
    }
    
    public void removePerm(String groupName, String permission) {
        
        Group group = luckPerms.getGroupManager().getGroup(groupName);
        //CachedMetaData metaData = playerAdapter.getMetaData(playerRef);
        
        assert group != null;
        group.data().remove(Node.builder(permission).build());
        luckPerms.getGroupManager().saveGroup(group);
        
    }
    
    public boolean getPerm(PlayerRef playerRef, String permission) {
        
        User user = playerAdapter.getUser(playerRef);
        //CachedPermissionData permissionData = playerAdapter.getPermissionData(playerRef);
        //CachedMetaData metaData = playerAdapter.getMetaData(playerRef);
        
        return user.getNodes().contains(Node.builder(permission).build());
        
    }
    
    public boolean getPerm(String groupName, String permission) {
        
        Group group = luckPerms.getGroupManager().getGroup(groupName);
        //CachedMetaData metaData = playerAdapter.getMetaData(playerRef);
        
        assert group != null;
        return group.getNodes().contains(Node.builder(permission).build());
        
    }

}
