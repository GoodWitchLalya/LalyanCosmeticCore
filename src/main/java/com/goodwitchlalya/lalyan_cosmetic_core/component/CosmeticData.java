package com.goodwitchlalya.lalyan_cosmetic_core.component;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CosmeticData implements Component<EntityStore> {
    
    public static ComponentType<EntityStore, CosmeticData> INSTANCE;
    
    private String[] internal = new String[0];
    
    public static final BuilderCodec<CosmeticData> CODEC = BuilderCodec.builder(CosmeticData.class, CosmeticData::new)
        .append(new KeyedCodec<>("Cosmetics", BuilderCodec.STRING_ARRAY), (data, value) -> data.internal = value, (data) -> data.internal)
        .add()
        .build();
    
    @NullableDecl
    @Override
    public Component<EntityStore> clone() {
        CosmeticData data = new CosmeticData();
        
        data.internal = internal;
        
        return data;
    }
    
    public List<String> getCosmetics() {
        return List.of(internal);
    }
    
    public void addCosmetic(String key) {
        List<String> list = new ArrayList<>(Arrays.asList(internal));
        list.add(key);
        
        internal = list.toArray(new String[0]);
    }
    
    public void removeCosmetic(String key) {
        List<String> list = new ArrayList<>(Arrays.asList(internal));
        list.remove(key);
        
        internal = list.toArray(new String[0]);
    }
}
