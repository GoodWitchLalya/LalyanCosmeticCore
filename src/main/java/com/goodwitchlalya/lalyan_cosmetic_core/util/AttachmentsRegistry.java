package com.goodwitchlalya.lalyan_cosmetic_core.util;

import com.goodwitchlalya.lalyan_cosmetic_core.CosmeticCore;
import com.goodwitchlalya.lalyan_cosmetic_core.component.CosmeticData;
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
import java.util.stream.Stream;


public class AttachmentsRegistry {
    
    private static AttachmentsRegistry INSTANCE;
    
    private final Map<String, Attachment> attachmentsRegistry = new HashMap<>();
    
    public enum SlotType {CHARACTER, COSMETIC}
    
    public interface Slot {
        SlotType getType();
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
        if(INSTANCE == null) INSTANCE = new AttachmentsRegistry();
        
        return INSTANCE;
    }
    
    public boolean isEmptySlot(Ref<EntityStore> ref, CosmeticSlot currentSlot) {
        Store<EntityStore> store = ref.getStore();
        CosmeticData data = store.getComponent(ref, CosmeticData.INSTANCE);
        
        if(data == null) return false;
        
        return data.getCosmetics().contains("No" + currentSlot.name());
    }
    
    public Map<String, Attachment> getAttachmentsRegistry() {
        return attachmentsRegistry;
    }
    
    public record Attachment(String name, ModelAttachment modelAttachment, Slot slot, String icon) {}
    
    private void restoreSkin(List<ModelAttachment> list, Map<String, Boolean> changes, PlayerSkin playerSkin) {
        CosmeticRegistry registry = CosmeticsModule.get().getRegistry();
        
        String gradientId = playerSkin.bodyCharacteristic.split("\\.")[1];
        String[] bodyCharacteristicParts = playerSkin.bodyCharacteristic.split("\\.");
        
        var bodyCharacteristic = registry.getBodyCharacteristics().get(bodyCharacteristicParts[0]);
        if (bodyCharacteristic != null) {
            list.add(ModelUtils.resolveAttachment(bodyCharacteristic, bodyCharacteristicParts, gradientId));
        }
        
        Map<String, Boolean> overrides = new HashMap<>(
            Stream.concat(
                    Arrays.stream(CharacterSlot.values()),
                    Arrays.stream(CosmeticSlot.values())
                )
                .collect(Collectors.toMap(Enum::name, _ -> false))
        );
        
        changes.forEach((cosmeticName, override) -> {
            String slotName = cosmeticName.replace("No", "");
            CosmeticSlot slot = null;
            
            try {
                slot = CosmeticSlot.valueOf(slotName);
            } catch (IllegalArgumentException ignored) {}
            
            if(slot != null) {
                overrides.replace(slot.toString(), override);
                return;
            }
            
            Attachment attachment = attachmentsRegistry.get(cosmeticName);
            if (attachment == null) {
                CosmeticCore.log("Attempting to apply changes to " + cosmeticName);
                return;
            }
            
            if (!override) {
                return;
            }
            
            overrides.replace(attachment.slot.toString(), true);
        });
        
        if (!overrides.get(CharacterSlot.Beards.name())) {
            if (playerSkin.facialHair != null) {
                String[] facialHairsParts = playerSkin.facialHair.split("\\.");
                var facialHairs = registry.getFacialHairs().get(facialHairsParts[0]);
                if (facialHairs != null) {
                    list.add(ModelUtils.resolveAttachment(facialHairs, facialHairsParts, gradientId));
                }
            }
        }
        
        if (!overrides.get(CharacterSlot.Ears.name())) {
            if (playerSkin.ears != null) {
                String[] earsParts = playerSkin.ears.split("\\.");
                var ears = registry.getEars().get(earsParts[0]);
                if (ears != null) {
                    list.add(ModelUtils.resolveAttachment(ears, earsParts, playerSkin.bodyCharacteristic.split("\\.")[1]));
                }
            }
        }
        
        if (!overrides.get(CharacterSlot.Eyebrows.name())) {
            if (playerSkin.eyebrows != null) {
                String[] eyebrowsParts = playerSkin.eyebrows.split("\\.");
                var eyebrows = registry.getEyebrows().get(eyebrowsParts[0]);
                if (eyebrows != null) {
                    list.add(ModelUtils.resolveAttachment(eyebrows, eyebrowsParts, gradientId));
                }
            }
        }
        
        if (!overrides.get(CharacterSlot.Eyes.name())) {
            if (playerSkin.eyes != null) {
                String[] eyesParts = playerSkin.eyes.split("\\.");
                var eyes = registry.getEyes().get(eyesParts[0]);
                if (eyes != null) {
                    list.add(ModelUtils.resolveAttachment(eyes, eyesParts, gradientId));
                }
            }
        }
        
        if (!overrides.get(CharacterSlot.Faces.name())) {
            if (playerSkin.face != null) {
                String[] faceParts = playerSkin.face.split("\\.");
                var face = registry.getFaces().get(faceParts[0]);
                if (face != null) {
                    list.add(ModelUtils.resolveAttachment(face, faceParts, playerSkin.bodyCharacteristic.split("\\.")[1]));
                }
            }
        }
        
        if (!overrides.get(CharacterSlot.Mouths.name())) {
            if (playerSkin.mouth != null) {
                String[] mouthsParts = playerSkin.mouth.split("\\.");
                var mouths = registry.getMouths().get(mouthsParts[0]);
                if (mouths != null) {
                    list.add(ModelUtils.resolveAttachment(mouths, mouthsParts, gradientId));
                }
            }
        }
        
        if (!overrides.get(CharacterSlot.Haircuts.name())) {
            if (playerSkin.haircut != null) {
                String[] haircutsParts = playerSkin.haircut.split("\\.");
                var haircuts = registry.getHaircuts().get(haircutsParts[0]);
                if (haircuts != null) {
                    list.add(ModelUtils.resolveAttachment(haircuts, haircutsParts, gradientId));
                }
            }
        }
        
        /* Cosmetics Slots */
        if (!overrides.get(CosmeticSlot.Capes.name())) {
            if (playerSkin.cape != null) {
                String[] capesParts = playerSkin.cape.split("\\.");
                var capes = registry.getCapes().get(capesParts[0]);
                if (capes != null) {
                    list.add(ModelUtils.resolveAttachment(capes, capesParts, gradientId));
                }
            }
        }
        
        if (!overrides.get(CosmeticSlot.Face_Accessories.name())) {
            if (playerSkin.faceAccessory != null) {
                String[] faceAccessoriesParts = playerSkin.faceAccessory.split("\\.");
                var faceAccessories = registry.getFaceAccessories().get(faceAccessoriesParts[0]);
                if (faceAccessories != null) {
                    list.add(ModelUtils.resolveAttachment(faceAccessories, faceAccessoriesParts, gradientId));
                }
            }
        }
        
        if (!overrides.get(CosmeticSlot.Gloves.name())) {
            if (playerSkin.gloves != null) {
                String[] glovesParts = playerSkin.gloves.split("\\.");
                var gloves = registry.getGloves().get(glovesParts[0]);
                if (gloves != null) {
                    list.add(ModelUtils.resolveAttachment(gloves, glovesParts, gradientId));
                }
            }
        }
        
        if (!overrides.get(CosmeticSlot.Head.name())) {
            if (playerSkin.headAccessory != null) {
                String[] headAccessoriesParts = playerSkin.headAccessory.split("\\.");
                var headAccessories = registry.getHeadAccessories().get(headAccessoriesParts[0]);
                if (headAccessories != null) {
                    list.add(ModelUtils.resolveAttachment(headAccessories, headAccessoriesParts, gradientId));
                }
            }
        }
        
        if (!overrides.get(CosmeticSlot.Overpants.name())) {
            if (playerSkin.overpants != null) {
                String[] overpantsParts = playerSkin.overpants.split("\\.");
                var overpants = registry.getOverpants().get(overpantsParts[0]);
                if (overpants != null) {
                    list.add(ModelUtils.resolveAttachment(overpants, overpantsParts, gradientId));
                }
            }
        }
        
        if (!overrides.get(CosmeticSlot.Overtops.name())) {
            if (playerSkin.overtop != null) {
                String[] overtopsParts = playerSkin.overtop.split("\\.");
                var overtops = registry.getOvertops().get(overtopsParts[0]);
                if (overtops != null) {
                    list.add(ModelUtils.resolveAttachment(overtops, overtopsParts, gradientId));
                }
            }
        }
        
        if (!overrides.get(CosmeticSlot.Pants.name())) {
            if (playerSkin.pants != null) {
                String[] pantsParts = playerSkin.pants.split("\\.");
                var pants = registry.getPants().get(pantsParts[0]);
                if (pants != null) {
                    list.add(ModelUtils.resolveAttachment(pants, pantsParts, gradientId));
                }
            }
        }
        
        if (!overrides.get(CosmeticSlot.Shoes.name())) {
            if (playerSkin.shoes != null) {
                String[] shoesParts = playerSkin.shoes.split("\\.");
                var shoes = registry.getShoes().get(shoesParts[0]);
                if (shoes != null) {
                    list.add(ModelUtils.resolveAttachment(shoes, shoesParts, gradientId));
                }
            }
        }
        
        if (!overrides.get(CosmeticSlot.Undertops.name())) {
            if (playerSkin.undertop != null) {
                String[] undertopsParts = playerSkin.undertop.split("\\.");
                var undertops = registry.getUndertops().get(undertopsParts[0]);
                if (undertops != null) {
                    list.add(ModelUtils.resolveAttachment(undertops, undertopsParts, gradientId));
                }
            }
        }
        
        if (!overrides.get(CosmeticSlot.Underwears.name())) {
            if (playerSkin.underwear != null) {
                String[] underwearParts = playerSkin.underwear.split("\\.");
                var underwear = registry.getUnderwear().get(underwearParts[0]);
                if (underwear != null) {
                    list.add(ModelUtils.resolveAttachment(underwear, underwearParts, gradientId));
                }
            }
        }
        
        if (!overrides.get(CosmeticSlot.Ears_Accessories.name())) {
            if (playerSkin.earAccessory != null) {
                String[] earAccessoriesParts = playerSkin.earAccessory.split("\\.");
                var earAccessories = registry.getEarAccessories().get(earAccessoriesParts[0]);
                if (earAccessories != null) {
                    list.add(ModelUtils.resolveAttachment(earAccessories, earAccessoriesParts, gradientId));
                }
            }
        }
        
        if (playerSkin.skinFeature != null) {
            String[] skinFeaturesParts = playerSkin.skinFeature.split("\\.");
            var skinFeatures = registry.getSkinFeatures().get(skinFeaturesParts[0]);
            if (skinFeatures != null) {
                list.add(ModelUtils.resolveAttachment(skinFeatures, skinFeaturesParts, gradientId));
            }
        }
    }
    
    public boolean containsChange(Ref<EntityStore> ref, String change) {
        Attachment attachment = attachmentsRegistry.getOrDefault(change, null);
        
        if(attachment == null) return false;
        
        Store<EntityStore> store = ref.getStore();
        
        CosmeticData data = store.getComponent(ref, CosmeticData.INSTANCE);
        
        if(data == null) {
            data = new CosmeticData();
            store.addComponent(ref, CosmeticData.INSTANCE, data);
            return false;
        }
        
        return data.getCosmetics().contains(change);
    }
    
    public void removeCosmetic(Ref<EntityStore> ref, String cosmeticId) {
        Store<EntityStore> store = ref.getStore();
        PlayerSkinComponent playerSkincomponent = store.getComponent(ref, PlayerSkinComponent.getComponentType());
        
        PlayerSkin playerSkinComponent = playerSkincomponent.getPlayerSkin();
        PlayerSkin playerSkin = playerSkinComponent.clone();
        Model playerModel = store.getComponent(ref, ModelComponent.getComponentType()).getModel();
        Player player = store.getComponent(ref, Player.getComponentType());
        
        List<ModelAttachment> list = new ArrayList<>();
        
        CosmeticData data = store.getComponent(ref, CosmeticData.INSTANCE);
        if(data == null) {
            data = new CosmeticData();
            store.addComponent(ref, CosmeticData.INSTANCE, data);
        }
        
        Map<String, Boolean> changes = new HashMap<>();
        changes = restoreFromData(ref, changes, false);
        restoreSkin(list, changes, playerSkin);
        
        Attachment attachment = attachmentsRegistry.getOrDefault(cosmeticId, null);
        if(attachment != null) {
            list.remove(attachment.modelAttachment);
        }
        
        data.removeCosmetic(cosmeticId);
        
        Model newModel = new Model(
            player.getDisplayName() + "CustomModel",
            playerModel.getScale(),
            playerModel.getRandomAttachmentIds(),
            list.toArray(new ModelAttachment[0]),
            playerModel.getBoundingBox(),
            playerModel.getModel(),
            playerModel.getTexture(),
            playerModel.getGradientSet(),
            playerModel.getGradientId(),
            playerModel.getEyeHeight(),
            playerModel.getCrouchOffset(),
            playerModel.getAnimationSetMap(),
            playerModel.getCamera(),
            playerModel.getLight(),
            playerModel.getParticles(),
            playerModel.getTrails(),
            playerModel.getPhysicsValues(),
            playerModel.getDetailBoxes(),
            playerModel.getPhobia(),
            playerModel.getPhobiaModelAssetId()
        );
        
        store.replaceComponent(ref, ModelComponent.getComponentType(), new ModelComponent(newModel));
        store.replaceComponent(ref, CosmeticData.INSTANCE, data);
    }
    
    private Map<String, Boolean> restoreFromData(Ref<EntityStore> ref, Map<String, Boolean> map, boolean override) {
        Store<EntityStore> store = ref.getStore();
        
        Map<String, Boolean> newM = new HashMap<>(map);
        
        CosmeticData data = store.getComponent(ref, CosmeticData.INSTANCE);
        
        if(data == null)  {
            data = new CosmeticData();
            store.addComponent(ref, CosmeticData.INSTANCE, data);
            return Map.of();
        }
        
        for (String cosmetic : data.getCosmetics()) {
            newM.put(cosmetic, override);
        }
        
        store.replaceComponent(ref, CosmeticData.INSTANCE, data);
        
        return newM;
    }
    
    public void addCosmetic(Ref<EntityStore> ref, String cosmeticId) {
        Store<EntityStore> store = ref.getStore();
        PlayerSkinComponent playerSkincomponent = store.getComponent(ref, PlayerSkinComponent.getComponentType());
        
        PlayerSkin playerSkinComponent = playerSkincomponent.getPlayerSkin();
        PlayerSkin playerSkin = playerSkinComponent.clone();
        Model playerModel = store.getComponent(ref, ModelComponent.getComponentType()).getModel();
        Player player = store.getComponent(ref, Player.getComponentType());
        
        List<ModelAttachment> list = new ArrayList<>();
        
        CosmeticData data = store.getComponent(ref, CosmeticData.INSTANCE);
        if(data == null) {
            data = new CosmeticData();
            store.addComponent(ref, CosmeticData.INSTANCE, data);
        }
        
        Map<String, Boolean> changes = new HashMap<>();
        
        changes.put(cosmeticId, true);
        
        changes = restoreFromData(ref, changes, true);
        restoreSkin(list, changes, playerSkin);
        
        Attachment attachment = attachmentsRegistry.getOrDefault(cosmeticId, null);
        if(attachment != null) {
            list.add(attachment.modelAttachment);
        }
        
        data.addCosmetic(cosmeticId);
        
        Model newModel = new Model(
            player.getDisplayName() + "CustomModel",
            playerModel.getScale(),
            playerModel.getRandomAttachmentIds(),
            list.toArray(new ModelAttachment[0]),
            playerModel.getBoundingBox(),
            playerModel.getModel(),
            playerModel.getTexture(),
            playerModel.getGradientSet(),
            playerModel.getGradientId(),
            playerModel.getEyeHeight(),
            playerModel.getCrouchOffset(),
            playerModel.getAnimationSetMap(),
            playerModel.getCamera(),
            playerModel.getLight(),
            playerModel.getParticles(),
            playerModel.getTrails(),
            playerModel.getPhysicsValues(),
            playerModel.getDetailBoxes(),
            playerModel.getPhobia(),
            playerModel.getPhobiaModelAssetId()
        );
        
        store.replaceComponent(ref, ModelComponent.getComponentType(), new ModelComponent(newModel));
        store.replaceComponent(ref, CosmeticData.INSTANCE, data);
    }
    
    public void applyChanges(Ref<EntityStore> ref, Map<String, Boolean> changes) {
        Store<EntityStore> store = ref.getStore();
        PlayerSkinComponent playerSkincomponent = store.getComponent(ref, PlayerSkinComponent.getComponentType());
        
        PlayerSkin playerSkinComponent = playerSkincomponent.getPlayerSkin();
        PlayerSkin playerSkin = playerSkinComponent.clone();
        Model playerModel = store.getComponent(ref, ModelComponent.getComponentType()).getModel();
        Player player = store.getComponent(ref, Player.getComponentType());
        
        List<ModelAttachment> list = new ArrayList<>();
        
        CosmeticData data = store.getComponent(ref, CosmeticData.INSTANCE);
        if(data == null) {
            data = new CosmeticData();
            store.addComponent(ref, CosmeticData.INSTANCE, data);
        }
        
        changes = restoreFromData(ref, changes, true);
        restoreSkin(list, changes, playerSkin);
        
        for (String key : changes.keySet()) {
            Attachment attachment = attachmentsRegistry.getOrDefault(key, null);
            if (attachment == null) continue;
            
            list.add(attachment.modelAttachment);
            data.addCosmetic(key);
        }
        
        Model newModel = new Model(
            player.getDisplayName() + "CustomModel",
            playerModel.getScale(),
            playerModel.getRandomAttachmentIds(),
            list.toArray(new ModelAttachment[0]),
            playerModel.getBoundingBox(),
            playerModel.getModel(),
            playerModel.getTexture(),
            playerModel.getGradientSet(),
            playerModel.getGradientId(),
            playerModel.getEyeHeight(),
            playerModel.getCrouchOffset(),
            playerModel.getAnimationSetMap(),
            playerModel.getCamera(),
            playerModel.getLight(),
            playerModel.getParticles(),
            playerModel.getTrails(),
            playerModel.getPhysicsValues(),
            playerModel.getDetailBoxes(),
            playerModel.getPhobia(),
            playerModel.getPhobiaModelAssetId()
        );
        
        store.replaceComponent(ref, ModelComponent.getComponentType(), new ModelComponent(newModel));
        store.replaceComponent(ref, CosmeticData.INSTANCE, data);
        
        Map<String, Boolean> overrides = new HashMap<>(
            Stream.concat(
                    Arrays.stream(CharacterSlot.values()),
                    Arrays.stream(CosmeticSlot.values())
                )
                .collect(Collectors.toMap(Enum::name, _ -> false))
        );
        
        CosmeticCore.log(
            changes.isEmpty() ?
                String.format("\nCosmetics applied to %s:\nNone (Default Skin)", player.getDisplayName()):
                String.format("\nCosmetics applied to %s:\n", player.getDisplayName()) +
                    changes.keySet().stream()
                        .map(attachmentsRegistry::get)
                        .filter(record -> record != null)
                        .map(record -> String.format("- %s (override: %s)", record.name(),  overrides.get(record.slot.toString())))
                        .collect(Collectors.joining("\n"))
        );
    }
    
    public void clearSlot(Ref<EntityStore> ref, CosmeticSlot currentSlot) {
        List<Map.Entry<String, Attachment>> attachments = attachmentsRegistry.entrySet()
            .stream()
            .filter(e -> e.getValue().slot == currentSlot)
            .toList();
        
        removeCosmetic(ref, "No"+currentSlot.name());
        
        for (Map.Entry<String, Attachment> attachment : attachments) {
            removeCosmetic(ref, attachment.getKey());
        }
    }
    
    public void applyChanges(Ref<EntityStore> ref) {
        applyChanges(ref, Map.of());
    }
    
    public void register(String name, Slot slot) {
        String attachmentPath = "Resources/";
        
        if (slot.getType() == SlotType.CHARACTER) {
            attachmentPath += "Characters/";
        } else if (slot.getType() == SlotType.COSMETIC) {
            attachmentPath += "Cosmetics/";
        }
        
        attachmentPath += String.format("%s/%s", slot, name);
        
        attachmentsRegistry.put(
            name,
            new Attachment(
                name,
                new ModelAttachment(
                    String.format("%s/%s.blockymodel", attachmentPath, name),
                    String.format("%s/%s.png", attachmentPath, name),
                    "",
                    "",
                    1
                ),
                slot,
                String.format("%s/Icon/%s.png", attachmentPath, name)
            )
        );
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
