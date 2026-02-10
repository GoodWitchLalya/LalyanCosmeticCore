package com.goodwitchlalya.lalyan_cosmetic_core.util;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class LccConfig {
    public static final BuilderCodec<LccConfig> CODEC = BuilderCodec.builder(LccConfig.class, LccConfig::new)
            .append(new KeyedCodec<Boolean>("Enable Permissions", Codec.BOOLEAN),
                    (config, value) -> config.permissions = value,// Setter
                    (config -> config.permissions)// Getter
            ).add()
            .build();
    
    private boolean permissions = false;
    
    public LccConfig() {}
    
    public boolean getPermissions() {
        return permissions;
    }
    
    public void setPermissions(boolean permissions) {
        this.permissions = permissions;
    }
}
