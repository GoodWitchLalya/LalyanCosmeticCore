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

/**
 * A custom component responsible for storing a player's equipped cosmetic data.
 * This data is attached to the player's entity and persists across sessions.
 */
public class CosmeticData implements Component<EntityStore> {
    
    // The unique type identifier for this component, used for registration and retrieval.
    public static ComponentType<EntityStore, CosmeticData> INSTANCE;
    
    // Internal array to store the string identifiers of the equipped cosmetics.
    private String[] internal = new String[0];
    
    /**
     * Codec for serializing and deserializing the component's data.
     * This allows the server to save and load the player's cosmetic choices.
     * It maps the "internal" array to a "Cosmetics" key in the saved data.
     */
    public static final BuilderCodec<CosmeticData> CODEC = BuilderCodec.builder(CosmeticData.class, CosmeticData::new)
        .append(new KeyedCodec<>("Cosmetics", BuilderCodec.STRING_ARRAY), (data, value) -> data.internal = value, (data) -> data.internal)
        .add()
        .build();
    
    /**
     * Creates a deep copy of this component.
     * This is required by the component system.
     *
     * @return A new CosmeticData instance with the same data.
     */
    @NullableDecl
    @Override
    public Component<EntityStore> clone() {
        CosmeticData data = new CosmeticData();
        // Copies the reference to the internal array. This is sufficient for immutable strings.
        data.internal = internal;
        
        return data;
    }
    
    /**
     * Returns an immutable list of the equipped cosmetic IDs.
     *
     * @return A List<String> containing the cosmetic IDs.
     */
    public List<String> getCosmetics() {
        return List.of(internal);
    }
    
    /**
     * Adds a cosmetic ID to the list of equipped cosmetics.
     *
     * @param key The unique string identifier of the cosmetic to add.
     */
    public void addCosmetic(String key) {
        List<String> list = new ArrayList<>(Arrays.asList(internal));
        list.add(key);
        
        internal = list.toArray(new String[0]);
    }
    
    /**
     * Removes a cosmetic ID from the list of equipped cosmetics.
     *
     * @param key The unique string identifier of the cosmetic to remove.
     */
    public void removeCosmetic(String key) {
        List<String> list = new ArrayList<>(Arrays.asList(internal));
        list.remove(key);
        
        internal = list.toArray(new String[0]);
    }
}
