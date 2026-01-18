package com.goodwitchlalya.lalyan_cosmetic_core.util;

import com.goodwitchlalya.lalyan_cosmetic_core.component.CosmeticData;
import com.google.gson.annotations.Expose;
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


public class AttachmentsRegistry {
    
    private static AttachmentsRegistry INSTANCE;
    
    private final Map<String, Attachment> attachmentsRegistry = new HashMap<>();
    
    public enum TopLevelTypes {Head, General, Torso, Legs, Capes}
    
    public enum SlotType {CHARACTER, COSMETIC}
    
    public interface Slot {
        SlotType getType();
        
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
    
    public enum CharacterSlot implements Slot {
        Beards, Ears, Eyebrows, Eyes, Faces, Mouths, Haircuts;
        
        @Override
        public SlotType getType() {
            return SlotType.CHARACTER;
        }
    }
    
    public enum CosmeticSlot implements Slot {
        Capes, Face_Accessories, Gloves, Head, Ears_Accessories, Overpants, Overtops, Pants, Shoes, Undertops, Underwears;
        
        @Override
        public SlotType getType() {
            return SlotType.COSMETIC;
        }
    }
    
    public static AttachmentsRegistry get() {
        if (INSTANCE == null) INSTANCE = new AttachmentsRegistry();
        
        return INSTANCE;
    }
    
    public Map<String, Attachment> getAttachmentsRegistry() {
        return attachmentsRegistry;
    }
    
    public static class AttachmentData {
        @Expose
        private final String model;
        @Expose
        private final String texture;
        @Expose
        private final String icon;
        
        public Slot slot;
        
        public AttachmentData(String model, String texture, String icon) {
            this.model = model;
            this.texture = texture;
            this.icon = icon;
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
    }
    
    public record Attachment(String name, AttachmentData data) {
        public ModelAttachment makeModel() {
            return new ModelAttachment(
                data.model(),
                data.texture(),
                "",
                "",
                1
            );
        }
    }
    
    public void rebuildSkinWithCosmetics(Ref<EntityStore> ref) {
        Store<EntityStore> store = ref.getStore();
        
        CosmeticData data = store.getComponent(ref, CosmeticData.INSTANCE);
        Player player = store.getComponent(ref, Player.getComponentType());
        Model model = store.getComponent(ref, ModelComponent.getComponentType()).getModel();
        
        if (data == null) {
            store.addComponent(ref, CosmeticData.INSTANCE, new CosmeticData());
            return;
        }
        
        List<ModelAttachment> attachments = new ArrayList<>();
        Map<Slot, Boolean> overrides = new HashMap<>();
        List<String> invalid = new ArrayList<>();
        
        for (String cosmetic : data.getCosmetics()) {
            Slot slot = Slot.valueOf(cosmetic.replace("No", ""));
            
            if(slot != null) {
                overrides.put(slot, true);
                continue;
            }
            
            Attachment attachment = attachmentsRegistry.get(cosmetic);
            if (attachment == null) {
                invalid.add(cosmetic);
                continue;
            }
            
            overrides.put(attachment.data().slot(), true);
            attachments.add(attachment.makeModel());
        }
        
        for (String inv : invalid) {
            data.removeCosmetic(inv);
        }
        
        restoreSkinWithOverrides(ref, attachments, overrides);
        store.replaceComponent(ref, CosmeticData.INSTANCE, data);
        
        Model newModel = new Model(player.getDisplayName() + "_CustomModel", model.getScale(), model.getRandomAttachmentIds(), attachments.toArray(new ModelAttachment[0]), model.getBoundingBox(), model.getModel(), model.getTexture(), model.getGradientSet(), model.getGradientId(), model.getEyeHeight(), model.getCrouchOffset(), model.getAnimationSetMap(), model.getCamera(), model.getLight(), model.getParticles(), model.getTrails(), model.getPhysicsValues(), model.getDetailBoxes(), model.getPhobia(), model.getPhobiaModelAssetId());
        
        store.replaceComponent(ref, ModelComponent.getComponentType(), new ModelComponent(newModel));
        store.replaceComponent(ref, CosmeticData.INSTANCE, data);
    }
    
    private void restoreSkinWithOverrides(Ref<EntityStore> ref, List<ModelAttachment> attachments, Map<Slot, Boolean> overrides) {
        CosmeticRegistry registry = CosmeticsModule.get().getRegistry();
        
        Store<EntityStore> store = ref.getStore();
        PlayerSkin playerSkin = store.getComponent(ref, PlayerSkinComponent.getComponentType()).getPlayerSkin();
        
        String gradientId = playerSkin.bodyCharacteristic.split("\\.")[1];
        String[] bodyCharacteristicParts = playerSkin.bodyCharacteristic.split("\\.");
        
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
    
    public boolean isEmptySlot(Ref<EntityStore> ref, Slot slot) {
        return containsChange(ref, "No" + slot.name());
    }
    
    public boolean containsChange(Ref<EntityStore> ref, String cosmeticId) {
        Store<EntityStore> store = ref.getStore();
        CosmeticData data = store.getComponent(ref, CosmeticData.INSTANCE);
        
        if (data == null) return false;
        
        return data.getCosmetics().contains(cosmeticId);
    }
    
    public void removeCosmetic(Ref<EntityStore> ref, String cosmeticId) {
        Store<EntityStore> store = ref.getStore();
        CosmeticData data = store.getComponent(ref, CosmeticData.INSTANCE);
        
        if (data == null) return;
        
        data.removeCosmetic(cosmeticId);
        rebuildSkinWithCosmetics(ref);
    }
    
    public void addCosmetic(Ref<EntityStore> ref, String cosmeticId, boolean override) {
        Store<EntityStore> store = ref.getStore();
        CosmeticData data = store.getComponent(ref, CosmeticData.INSTANCE);
        
        if (data == null) return;
        
        if (override) {
            clearSlot(ref, cosmeticId);
        }
        
        data.addCosmetic(cosmeticId);
        rebuildSkinWithCosmetics(ref);
    }
    
    public void clearSlot(Ref<EntityStore> ref, String cosmeticId) {
        Attachment attachment = attachmentsRegistry.get(cosmeticId);
        
        if (attachment == null) return;
        if (attachment.data().slot().getType() != SlotType.COSMETIC) return;
        
        clearSlot(ref, attachment.data().slot());
    }
    
    public void clearSlot(Ref<EntityStore> ref, Slot slot) {
        Store<EntityStore> store = ref.getStore();
        CosmeticData data = store.getComponent(ref, CosmeticData.INSTANCE);
        
        if (data == null) return;
        
        List<String> toRemove = new ArrayList<>();
        
        for (String cosmetic : data.getCosmetics()) {
            if (cosmetic.contains("No" + slot.name())) {
                toRemove.add(cosmetic);
                continue;
            }
            
            Attachment attachment = attachmentsRegistry.get(cosmetic);
            
            if (attachment == null) {
                toRemove.add(cosmetic);
                continue;
            }
            
            if (attachment.data().slot() == slot) {
                toRemove.add(cosmetic);
            }
        }
        
        for (String rev : toRemove) {
            data.removeCosmetic(rev);
        }
        
        rebuildSkinWithCosmetics(ref);
    }
    
    public void clearCosmetics(Ref<EntityStore> ref) {
        for (CosmeticSlot slot : CosmeticSlot.values()) {
            clearSlot(ref, slot);
        }
    }
    
    public void clearCharacter(Ref<EntityStore> ref) {
        for (CharacterSlot slot : CharacterSlot.values()) {
            clearSlot(ref, slot);
        }
    }
    
    public void clearAll(Ref<EntityStore> ref) {
        clearCosmetics(ref);
        clearCharacter(ref);
    }
    
    public void register(String name, Slot slot) {
        String attachmentPath = "Resources/";
        
        if (slot.getType() == SlotType.CHARACTER) {
            attachmentPath += "Characters/";
        } else if (slot.getType() == SlotType.COSMETIC) {
            attachmentPath += "Cosmetics/";
        }
        
        attachmentPath += String.format("%s/%s", slot, name);
        
        AttachmentData attData = new AttachmentData(String.format("%s/%s.blockymodel", attachmentPath, name), String.format("%s/%s.png", attachmentPath, name), String.format("%s/Icon/%s.png", attachmentPath, name));
        attData.slot = slot;
        
        register(name, attData);
    }
    
    public void register(String name, AttachmentData attachmentData) {
        attachmentsRegistry.put(name, new Attachment(name, attachmentData));
    }
    
    public String getKey(Attachment e) {
        for (Map.Entry<String, Attachment> entry : attachmentsRegistry.entrySet()) {
            if (entry.getValue() == e) {
                return entry.getKey();
            }
        }
        
        return null;
    }
    
    public void clear() {
        attachmentsRegistry.clear();
    }
    
    public List<String> getAttachmentsList() {
        return attachmentsRegistry.keySet().stream().sorted().collect(Collectors.toList());
    }
    
}
