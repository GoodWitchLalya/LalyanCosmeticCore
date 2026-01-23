package com.goodwitchlalya.lalyan_cosmetic_core.util;

import com.goodwitchlalya.lalyan_cosmetic_core.CosmeticCore;
import com.goodwitchlalya.lalyan_cosmetic_core.component.CosmeticData;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.PlayerSkin;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAttachment;
import com.hypixel.hytale.server.core.cosmetics.CosmeticRegistry;
import com.hypixel.hytale.server.core.cosmetics.CosmeticsModule;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSkinComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.*;
import java.util.stream.Collectors;

// A singleton registry that manages all custom cosmetic and character attachments.
// It handles loading, storing, and applying these attachments to player models.
public class AttachmentsRegistry {
    
    // Singleton instance of the registry.
    private static AttachmentsRegistry INSTANCE;
    
    // The main map storing all registered attachments, keyed by a unique ID (e.g., "assetpack#cosmeticName").
    private final Map<String, Attachment> attachmentsRegistry = new HashMap<>();
    
    // A list of slots that doesn't make the override by default
    public final List<Slot> nonOverridingSlots = List.of(
            CharacterSlot.Hair_Extension
    );
    
    // A list of slots that use the same hair gradient by default
    public final List<Slot> hairColouredSlots = List.of(
            CharacterSlot.Hair_Extension
    );
    
    // Enum for top-level UI categories.
    public enum TopLevelTypes {Head, General, Torso, Legs, Capes, All}
    
    // Enum to differentiate between character parts and wearable cosmetics.
    public enum SlotType {CHARACTER, COSMETIC}
    
    // Common interface for all cosmetic/character slots.
    // Provides a way to group and handle different slot types polymorphically.
    public interface Slot {
        SlotType getType();
        
        // A utility method to find a Slot enum constant by its name,
        // searching through both CosmeticSlot and CharacterSlot.
        static Slot valueOf(String name) {
            try {
                return CosmeticSlot.valueOf(name);
            } catch (IllegalArgumentException _) {
                try {
                    return CharacterSlot.valueOf(name);
                } catch (IllegalArgumentException e) {
                    return null;
                }
            }
        }
        
        String name();
    }
    
    // Enum representing slots for built-in character features like eyes and hair.
    public enum CharacterSlot implements Slot {
        Beards, Ears, Eyebrows, Eyes, Faces, Mouths, Haircuts, Hair_Extension;
        
        @Override
        public SlotType getType() {
            return SlotType.CHARACTER;
        }
    }
    
    // Enum representing slots for wearable cosmetics like hats and capes.
    public enum CosmeticSlot implements Slot {
        Capes, Face_Accessories, Gloves, Head, Ears_Accessories, Overpants, Overtops, Pants, Shoes, Undertops, Underwears;
        
        @Override
        public SlotType getType() {
            return SlotType.COSMETIC;
        }
    }
    
    // Provides access to the singleton instance of the registry.
    public static AttachmentsRegistry get() {
        if (INSTANCE == null) INSTANCE = new AttachmentsRegistry();
        
        return INSTANCE;
    }
    
    // Returns the raw map of registered attachments.
    public Map<String, Attachment> getAttachmentsRegistry() {
        return attachmentsRegistry;
    }
    
    // A record to hold data for a single cosmetic variant (texture and icon).
    public record Variant(@Expose String texture, @Expose String icon) {
    
    }
    
    // A class holding all the data for a single attachment, loaded from asset files or JSON.
    // This includes paths to model, texture, icon, as well as variants and slot overrides.
    public static class AttachmentData {
        @Expose
        private final String model;
        @Expose
        private final String texture;
        @Expose
        private final String icon;
        @Expose
        private final Map<String, Variant> variants;
        @Expose
        @SerializedName("slot_overrides")
        private final List<String> slotOverrides;
        
        public Slot slot; // The primary slot this attachment belongs to.
        
        public AttachmentData(String model, String texture, String icon, Map<String, Variant> variants, List<String> slotOverrides) {
            this.model = model;
            this.texture = texture;
            this.icon = icon;
            this.variants = variants;
            this.slotOverrides = slotOverrides;
        }
        
        public String model() {
            return model;
        }
        
        public String texture() {
            return texture;
        }
        
        public String icon() {
            return icon;
        }
        
        public Slot slot() {
            return slot;
        }
        
        public Map<String, Variant> variants() {
            return variants;
        }
        
        public List<String> slotOverrides() {
            return slotOverrides != null? slotOverrides: List.of();
        }
    }
    
    // A record representing a fully processed attachment, containing its name and its data.
    public record Attachment(String name, AttachmentData data) {
        // Creates a Hytale ModelAttachment object from this attachment's data.
        // @param variant The name of the variant to use. If empty, the default texture is used.
        // @return A ModelAttachment ready to be applied to a player model.
        public ModelAttachment makeModel(String variant, String gradientSet, String gradientID) {
            if(variant.isEmpty()) {
                return new ModelAttachment(
                    data.model(),
                    data.texture(),
                    gradientSet,
                    gradientID,
                    1
                );
            }
            
            Variant v = data.variants.get(variant);
            
            return new ModelAttachment(
                data.model(),
                v.texture(),
                gradientSet,
                gradientID,
                1
            );
        }
        
        public ModelAttachment makeModel(String variant) {
            return makeModel(variant, "", "");
        }
    }
    
    // The core method for rebuilding a player's skin. It combines the player's default skin
    // with the custom cosmetics they have equipped.
    // @param ref A reference to the player entity.
    public void rebuildSkinWithCosmetics(Ref<EntityStore> ref) {
        Store<EntityStore> store = ref.getStore();
        
        CosmeticData data = store.getComponent(ref, CosmeticData.INSTANCE);
        Player player = store.getComponent(ref, Player.getComponentType());
        Model model = store.getComponent(ref, ModelComponent.getComponentType()).getModel();
        PlayerSkin playerSkin = store.getComponent(ref, PlayerSkinComponent.getComponentType()).getPlayerSkin();
        
        if (data == null) {
            store.addComponent(ref, CosmeticData.INSTANCE, new CosmeticData());
            return;
        }
        
        List<ModelAttachment> attachments = new ArrayList<>();
        Map<Slot, Boolean> overrides = new HashMap<>();
        List<String> invalid = new ArrayList<>();
        
        // Iterate through the player's equipped cosmetics.
        for (String cosmetic : data.getCosmetics()) {
            // Handle "empty slot" markers.
            Slot slot = Slot.valueOf(cosmetic.replace("No", ""));
            
            if(slot != null) {
                overrides.put(slot, true);
                continue;
            }
            
            // Parse cosmetic ID and variant name.
            String cosmId = cosmetic;
            String variant = "";
            
            if(cosmetic.contains("$")) {
                String[] split = cosmetic.split("\\$");
                cosmId = split[0];
                variant = split[1];
            }
            
            // Find the attachment in the registry.
            Attachment attachment = attachmentsRegistry.get(cosmId);
            if (attachment == null) {
                invalid.add(cosmId);
                continue;
            }
            
            // Add the attachment's primary slot and any extra override slots to the override map.
            if (nonOverridingSlots.contains(attachment.data().slot())) {
                overrides.put(attachment.data().slot(), false);
            } else {
                overrides.put(attachment.data().slot(), true);
            }
            
            for (String override : attachment.data().slotOverrides()) {
                Slot overrideSlot = Slot.valueOf(override);
                if (overrideSlot != null) {
                    overrides.put(overrideSlot, true);
                }
            }
            
            // Create the model attachment and add it to the list.
            if (hairColouredSlots.contains(attachment.data().slot())) {
                String gradientSet = "Hair";
                String gradientId = playerSkin.haircut.split("\\.")[1];
                attachments.add(attachment.makeModel(variant, gradientSet, gradientId));
            } else {
                attachments.add(attachment.makeModel(variant));
            }
        }
        
        // Clean up any invalid cosmetics from the player's data.
        for (String inv : invalid) {
            data.removeCosmetic(inv);
        }
        
        // Restore the base skin, skipping parts that are overridden by custom cosmetics.
        restoreSkinWithOverrides(ref, attachments, overrides);
        store.replaceComponent(ref, CosmeticData.INSTANCE, data);
        
        // Create a new player model with the combined attachments.
        Model newModel = new Model(player.getDisplayName() + "_CustomModel", model.getScale(), model.getRandomAttachmentIds(), attachments.toArray(new ModelAttachment[0]), model.getBoundingBox(), model.getModel(), model.getTexture(), model.getGradientSet(), model.getGradientId(), model.getEyeHeight(), model.getCrouchOffset(), model.getAnimationSetMap(), model.getCamera(), model.getLight(), model.getParticles(), model.getTrails(), model.getPhysicsValues(), model.getDetailBoxes(), model.getPhobia(), model.getPhobiaModelAssetId());
        
        // Apply the new model to the player.
        store.replaceComponent(ref, ModelComponent.getComponentType(), new ModelComponent(newModel));
        store.replaceComponent(ref, CosmeticData.INSTANCE, data);
    }
    
    // Re-applies the player's default Hytale skin parts (hair, eyes, etc.) unless they
    // are marked as being overridden by a custom cosmetic.
    private void restoreSkinWithOverrides(Ref<EntityStore> ref, List<ModelAttachment> attachments, Map<Slot, Boolean> overrides) {
        CosmeticRegistry registry = CosmeticsModule.get().getRegistry();
        
        Store<EntityStore> store = ref.getStore();
        PlayerSkin playerSkin = store.getComponent(ref, PlayerSkinComponent.getComponentType()).getPlayerSkin();
        
        String gradientId = playerSkin.bodyCharacteristic.split("\\.")[1];
        String[] bodyCharacteristicParts = playerSkin.bodyCharacteristic.split("\\.");
        
        // This large block of code checks each vanilla cosmetic slot. If it's not in the 'overrides' map,
        // it resolves the corresponding attachment from the vanilla registry and adds it to the list.
        var bodyCharacteristic = registry.getBodyCharacteristics().get(bodyCharacteristicParts[0]);
        if (bodyCharacteristic != null) {
            attachments.add(ModelUtils.resolveAttachment(bodyCharacteristic, bodyCharacteristicParts, gradientId));
        }
        
        if (!overrides.getOrDefault(CharacterSlot.Beards, false)) {
            if (playerSkin.facialHair != null) {
                String[] facialHairsParts = playerSkin.facialHair.split("\\.");
                var facialHairs = registry.getFacialHairs().get(facialHairsParts[0]);
                if (facialHairs != null) {
                    attachments.add(ModelUtils.resolveAttachment(facialHairs, facialHairsParts, gradientId));
                }
            }
        }
        
        if (!overrides.getOrDefault(CharacterSlot.Ears, false)) {
            if (playerSkin.ears != null) {
                String[] earsParts = playerSkin.ears.split("\\.");
                var ears = registry.getEars().get(earsParts[0]);
                if (ears != null) {
                    attachments.add(ModelUtils.resolveAttachment(ears, earsParts, playerSkin.bodyCharacteristic.split("\\.")[1]));
                }
            }
        }
        
        if (!overrides.getOrDefault(CharacterSlot.Eyebrows, false)) {
            if (playerSkin.eyebrows != null) {
                String[] eyebrowsParts = playerSkin.eyebrows.split("\\.");
                var eyebrows = registry.getEyebrows().get(eyebrowsParts[0]);
                if (eyebrows != null) {
                    attachments.add(ModelUtils.resolveAttachment(eyebrows, eyebrowsParts, gradientId));
                }
            }
        }
        
        if (!overrides.getOrDefault(CharacterSlot.Eyes, false)) {
            if (playerSkin.eyes != null) {
                String[] eyesParts = playerSkin.eyes.split("\\.");
                var eyes = registry.getEyes().get(eyesParts[0]);
                if (eyes != null) {
                    attachments.add(ModelUtils.resolveAttachment(eyes, eyesParts, gradientId));
                }
            }
        }
        
        if (!overrides.getOrDefault(CharacterSlot.Faces, false)) {
            if (playerSkin.face != null) {
                String[] faceParts = playerSkin.face.split("\\.");
                var face = registry.getFaces().get(faceParts[0]);
                if (face != null) {
                    attachments.add(ModelUtils.resolveAttachment(face, faceParts, playerSkin.bodyCharacteristic.split("\\.")[1]));
                }
            }
        }
        
        if (!overrides.getOrDefault(CharacterSlot.Mouths, false)) {
            if (playerSkin.mouth != null) {
                String[] mouthsParts = playerSkin.mouth.split("\\.");
                var mouths = registry.getMouths().get(mouthsParts[0]);
                if (mouths != null) {
                    attachments.add(ModelUtils.resolveAttachment(mouths, mouthsParts, gradientId));
                }
            }
        }
        
        if (!overrides.getOrDefault(CharacterSlot.Haircuts, false)) {
            if (playerSkin.haircut != null) {
                String[] haircutsParts = playerSkin.haircut.split("\\.");
                var haircuts = registry.getHaircuts().get(haircutsParts[0]);
                if (haircuts != null) {
                    attachments.add(ModelUtils.resolveAttachment(haircuts, haircutsParts, gradientId));
                }
            }
        }
        
        /* Cosmetics Slots */
        if (!overrides.getOrDefault(CosmeticSlot.Capes, false)) {
            if (playerSkin.cape != null) {
                String[] capesParts = playerSkin.cape.split("\\.");
                var capes = registry.getCapes().get(capesParts[0]);
                if (capes != null) {
                    attachments.add(ModelUtils.resolveAttachment(capes, capesParts, gradientId));
                }
            }
        }
        
        if (!overrides.getOrDefault(CosmeticSlot.Face_Accessories, false)) {
            if (playerSkin.faceAccessory != null) {
                String[] faceAccessoriesParts = playerSkin.faceAccessory.split("\\.");
                var faceAccessories = registry.getFaceAccessories().get(faceAccessoriesParts[0]);
                if (faceAccessories != null) {
                    attachments.add(ModelUtils.resolveAttachment(faceAccessories, faceAccessoriesParts, gradientId));
                }
            }
        }
        
        if (!overrides.getOrDefault(CosmeticSlot.Gloves, false)) {
            if (playerSkin.gloves != null) {
                String[] glovesParts = playerSkin.gloves.split("\\.");
                var gloves = registry.getGloves().get(glovesParts[0]);
                if (gloves != null) {
                    attachments.add(ModelUtils.resolveAttachment(gloves, glovesParts, gradientId));
                }
            }
        }
        
        if (!overrides.getOrDefault(CosmeticSlot.Head, false)) {
            if (playerSkin.headAccessory != null) {
                String[] headAccessoriesParts = playerSkin.headAccessory.split("\\.");
                var headAccessories = registry.getHeadAccessories().get(headAccessoriesParts[0]);
                if (headAccessories != null) {
                    attachments.add(ModelUtils.resolveAttachment(headAccessories, headAccessoriesParts, gradientId));
                }
            }
        }
        
        if (!overrides.getOrDefault(CosmeticSlot.Overpants, false)) {
            if (playerSkin.overpants != null) {
                String[] overpantsParts = playerSkin.overpants.split("\\.");
                var overpants = registry.getOverpants().get(overpantsParts[0]);
                if (overpants != null) {
                    attachments.add(ModelUtils.resolveAttachment(overpants, overpantsParts, gradientId));
                }
            }
        }
        
        if (!overrides.getOrDefault(CosmeticSlot.Overtops, false)) {
            if (playerSkin.overtop != null) {
                String[] overtopsParts = playerSkin.overtop.split("\\.");
                var overtops = registry.getOvertops().get(overtopsParts[0]);
                if (overtops != null) {
                    attachments.add(ModelUtils.resolveAttachment(overtops, overtopsParts, gradientId));
                }
            }
        }
        
        if (!overrides.getOrDefault(CosmeticSlot.Pants, false)) {
            if (playerSkin.pants != null) {
                String[] pantsParts = playerSkin.pants.split("\\.");
                var pants = registry.getPants().get(pantsParts[0]);
                if (pants != null) {
                    attachments.add(ModelUtils.resolveAttachment(pants, pantsParts, gradientId));
                }
            }
        }
        
        if (!overrides.getOrDefault(CosmeticSlot.Shoes, false)) {
            if (playerSkin.shoes != null) {
                String[] shoesParts = playerSkin.shoes.split("\\.");
                var shoes = registry.getShoes().get(shoesParts[0]);
                if (shoes != null) {
                    attachments.add(ModelUtils.resolveAttachment(shoes, shoesParts, gradientId));
                }
            }
        }
        
        if (!overrides.getOrDefault(CosmeticSlot.Undertops, false)) {
            if (playerSkin.undertop != null) {
                String[] undertopsParts = playerSkin.undertop.split("\\.");
                var undertops = registry.getUndertops().get(undertopsParts[0]);
                if (undertops != null) {
                    attachments.add(ModelUtils.resolveAttachment(undertops, undertopsParts, gradientId));
                }
            }
        }
        
        if (!overrides.getOrDefault(CosmeticSlot.Underwears, false)) {
            if (playerSkin.underwear != null) {
                String[] underwearParts = playerSkin.underwear.split("\\.");
                var underwear = registry.getUnderwear().get(underwearParts[0]);
                if (underwear != null) {
                    attachments.add(ModelUtils.resolveAttachment(underwear, underwearParts, gradientId));
                }
            }
        }
        
        if (!overrides.getOrDefault(CosmeticSlot.Ears_Accessories, false)) {
            if (playerSkin.earAccessory != null) {
                String[] earAccessoriesParts = playerSkin.earAccessory.split("\\.");
                var earAccessories = registry.getEarAccessories().get(earAccessoriesParts[0]);
                if (earAccessories != null) {
                    attachments.add(ModelUtils.resolveAttachment(earAccessories, earAccessoriesParts, gradientId));
                }
            }
        }
        
        if (playerSkin.skinFeature != null) {
            String[] skinFeaturesParts = playerSkin.skinFeature.split("\\.");
            var skinFeatures = registry.getSkinFeatures().get(skinFeaturesParts[0]);
            if (skinFeatures != null) {
                attachments.add(ModelUtils.resolveAttachment(skinFeatures, skinFeaturesParts, gradientId));
            }
        }
    }
    
    // Checks if a player has marked a specific slot as empty.
    public boolean isEmptySlot(Ref<EntityStore> ref, Slot slot) {
        return containsChange(ref, "No" + slot.name());
    }
    
    // Checks if a player has a specific cosmetic or one of its variants equipped.
    public boolean containsChange(Ref<EntityStore> ref, String cosmeticId) {
        Store<EntityStore> store = ref.getStore();
        CosmeticData data = store.getComponent(ref, CosmeticData.INSTANCE);
        
        if (data == null) return false;
        
        for (String cosmetic : data.getCosmetics()) {
            if (cosmetic.equals(cosmeticId)) return true;
            if (cosmetic.startsWith(cosmeticId + "$")) return true;
        }
        
        return false;
    }
    
    // Gets the name of the equipped variant for a given base cosmetic ID.
    // @return The variant name, or null if no variant is equipped.
    public String getEquippedVariant(Ref<EntityStore> ref, String cosmeticId) {
        Store<EntityStore> store = ref.getStore();
        CosmeticData data = store.getComponent(ref, CosmeticData.INSTANCE);
        
        if (data == null) return null;
        
        for (String cosmetic : data.getCosmetics()) {
            if (cosmetic.startsWith(cosmeticId + "$")) {
                return cosmetic.split("\\$")[1];
            }
        }
        
        return null;
    }
    
    // Checks if a player has a specific cosmetic ID (including variant) equipped.
    public boolean isEquipped(Ref<EntityStore> ref, String cosmeticId) {
        Store<EntityStore> store = ref.getStore();
        CosmeticData data = store.getComponent(ref, CosmeticData.INSTANCE);
        
        if (data == null) return false;
        
        return data.getCosmetics().contains(cosmeticId);
    }
    
    // Removes a cosmetic (and any of its variants) from a player.
    public void removeCosmetic(Ref<EntityStore> ref, String cosmeticId) {
        Store<EntityStore> store = ref.getStore();
        CosmeticData data = store.getComponent(ref, CosmeticData.INSTANCE);
        
        if (data == null) return;
        
        // Find all cosmetics to remove (base ID and any variants).
        List<String> toRemove = new ArrayList<>();
        
        for (String cosmetic : data.getCosmetics()) {
            if (cosmetic.equals(cosmeticId) || cosmetic.startsWith(cosmeticId + "$")) {
                toRemove.add(cosmetic);
            }
        }
        
        for (String s : toRemove) {
            data.removeCosmetic(s);
        }
        
        // Handle slot overrides removal
        // If we are removing a "No" cosmetic, we need to check if other cosmetics depended on it.
        // This logic seems complex and might need review.
        if (cosmeticId.startsWith("No")) {
            Slot slot = Slot.valueOf(cosmeticId.replace("No", ""));
            if (slot != null) {
                List<String> dependentCosmetics = new ArrayList<>();
                for (String cosmetic : data.getCosmetics()) {
                    if (cosmetic.startsWith("No")) continue;
                    
                    String id = cosmetic.split("\\$")[0];
                    Attachment attachment = attachmentsRegistry.get(id);
                    if (attachment != null && attachment.data().slotOverrides().contains(slot.name())) {
                        dependentCosmetics.add(cosmetic);
                    }
                }
                
                for (String dep : dependentCosmetics) {
                    removeCosmetic(ref, dep);
                }
            }
        } else {
            // If we remove a regular cosmetic, also remove any "No" markers it might have added.
            String id = cosmeticId.split("\\$")[0];
            Attachment attachment = attachmentsRegistry.get(id);
            if (attachment != null) {
                for (String override : attachment.data().slotOverrides()) {
                    data.removeCosmetic("No" + override);
                }
            }
        }
        
        rebuildSkinWithCosmetics(ref);
    }
    
    // Adds a cosmetic to a player.
    // @param override If true, clears the slot before adding the new cosmetic.
    public void addCosmetic(Ref<EntityStore> ref, String cosmeticId, boolean override) {
        String cosmId = cosmeticId;
        
        if(cosmeticId.contains("$")) {
            String[] split = cosmeticId.split("\\$");
            cosmId = split[0];
        }
        
        Store<EntityStore> store = ref.getStore();
        CosmeticData data = store.getComponent(ref, CosmeticData.INSTANCE);
        
        if (data == null) return;
        
        if (override) {
            clearSlot(ref, cosmId);
        }
        
        // Add any necessary slot overrides for this cosmetic.
        Attachment attachment = attachmentsRegistry.get(cosmId);
        if (attachment != null) {
            for (String overrideSlot : attachment.data().slotOverrides()) {
                clearSlot(ref, Slot.valueOf(overrideSlot));
                data.addCosmetic("No" + overrideSlot);
            }
        }
        
        data.addCosmetic(cosmeticId);
        rebuildSkinWithCosmetics(ref);
    }
    
    // Clears all cosmetics from a given slot, determined by a cosmetic ID.
    public void clearSlot(Ref<EntityStore> ref, String cosmeticId) {
        String id = cosmeticId;
        
        if (id.contains("$")) {
            id = id.split("\\$")[0];
        }
        
        if (cosmeticId.contains("No")) {
            clearSlot(ref, Slot.valueOf(cosmeticId.replace("No", "")));
            return;
        }
        
        Attachment attachment = attachmentsRegistry.get(id);
        
        if (attachment == null) return;
        
        clearSlot(ref, attachment.data().slot());
    }
    
    // Clears all cosmetics from a specific slot.
    public void clearSlot(Ref<EntityStore> ref, Slot slot) {
        Store<EntityStore> store = ref.getStore();
        CosmeticData data = store.getComponent(ref, CosmeticData.INSTANCE);
        
        if (data == null) return;
        
        List<String> toRemove = new ArrayList<>();
        
        // Find all cosmetics belonging to the specified slot.
        for (String cosmetic : data.getCosmetics()) {
            if (cosmetic.equals("No" + slot.name())) {
                toRemove.add(cosmetic);
                continue;
            }
            
            String id = cosmetic;
            if (id.contains("$")) {
                id = id.split("\\$")[0];
            }
            
            Attachment attachment = attachmentsRegistry.get(id);
            
            if (attachment == null) {
                if (cosmetic.startsWith("No")) {
                    continue;
                }
                
                // Remove invalid cosmetics.
                toRemove.add(cosmetic);
                continue;
            }
            
            if (attachment.data().slot() == slot) {
                toRemove.add(cosmetic);
            }
        }
        
        // Remove them.
        for (String rev : toRemove) {
            removeCosmetic(ref, rev);
        }
        
        rebuildSkinWithCosmetics(ref);
    }
    
    // Clears all wearable cosmetics from the player.
    public void clearCosmetics(Ref<EntityStore> ref) {
        for (CosmeticSlot slot : CosmeticSlot.values()) {
            clearSlot(ref, slot);
        }
    }
    
    // Clears all character parts from the player.
    public void clearCharacter(Ref<EntityStore> ref) {
        for (CharacterSlot slot : CharacterSlot.values()) {
            clearSlot(ref, slot);
        }
    }
    
    // Clears all custom attachments (both cosmetics and character parts) from the player.
    public void clearAll(Ref<EntityStore> ref) {
        clearCosmetics(ref);
        clearCharacter(ref);
    }
    
    // Registers a new attachment with default paths and no variants.
    public void register(String name, Slot slot) {
        register(name, slot, Map.of());
    }
    
    // Registers a new attachment with default paths but with specified variants.
    public void register(String name, Slot slot, Map<String, Variant> variants) {
        String attachmentPath = "Resources/";
        
        if (slot.getType() == SlotType.CHARACTER) {
            attachmentPath += "Characters/";
        } else if (slot.getType() == SlotType.COSMETIC) {
            attachmentPath += "Cosmetics/";
        }
        
        String[] split = name.split("#");
        
        
        attachmentPath += String.format("%s/%s", slot, split[1]);
        
        // Create AttachmentData with conventional paths.
        AttachmentData attData = new AttachmentData(
                String.format("%s/%s.blockymodel", attachmentPath, split[1]),
                String.format("%s/%s.png", attachmentPath, split[1]),
                String.format("%s/Icon/%s.png", attachmentPath, split[1]),
                variants,
                List.of()
        );
        attData.slot = slot;
        
        register(name, attData);
    }
    
    // The core registration method. Adds a fully-formed AttachmentData object to the registry.
    public void register(String name, AttachmentData attachmentData) {
        attachmentsRegistry.put(name, new Attachment(name, attachmentData));
    }
    
    // Utility method to find the key for a given Attachment object.
    public String getKey(Attachment e) {
        for (Map.Entry<String, Attachment> entry : attachmentsRegistry.entrySet()) {
            if (entry.getValue() == e) {
                return entry.getKey();
            }
        }
        
        return null;
    }
    
    // Clears all attachments from the registry. Used during reload.
    public void clear() {
        attachmentsRegistry.clear();
    }
    
    // Returns a sorted list of all registered cosmetic IDs.
    public List<String> getAttachmentsList() {
        return attachmentsRegistry.keySet().stream().sorted().collect(Collectors.toList());
    }
    
}