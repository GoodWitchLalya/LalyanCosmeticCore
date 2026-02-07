package com.goodwitchlalya.lalyan_cosmetic_core.util;

import com.goodwitchlalya.lalyan_cosmetic_core.CosmeticCore;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;

public class LuckpermsCompatibility {

    private static LuckpermsCompatibility instance;
    
    private static LuckPerms luckPerms = LuckPermsProvider.get();

    private LuckpermsCompatibility() {
    
    }

    public static LuckpermsCompatibility getInstance() {
        if (instance == null) {
            instance = new LuckpermsCompatibility();
        }
        return instance;
    }

}
