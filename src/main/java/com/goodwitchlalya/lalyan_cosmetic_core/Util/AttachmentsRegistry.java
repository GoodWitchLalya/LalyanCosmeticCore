package com.goodwitchlalya.lalyan_cosmetic_core.Util;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.PlayerSkin;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAttachment;
import com.hypixel.hytale.server.core.cosmetics.CosmeticRegistry;
import com.hypixel.hytale.server.core.cosmetics.CosmeticsModule;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSkinComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.*;

public class AttachmentsRegistry {
    
    private static AttachmentsRegistry instance;
    
    private static final Map<String, Attachment> attachmentsRegistry = new HashMap<String, Attachment>();
    
    public enum SlotType { CHARACTER, COSMETIC }
    public interface Slot {
        SlotType getType();
    }
    
    public enum CharacterSlot implements Slot {
        Beard, Ears, Eyebrows, Eyes, Face, Mouth, Haircut;
        
        @Override
        public SlotType getType() {
            return SlotType.CHARACTER;
        }
    };
    public enum CosmeticSlot implements Slot {
        Arms, Cape, Face_Accessory, Gloves, Head, Overpant, Overtop, Pants, Shoes, Undertop, Underwear;
        
        @Override
        public SlotType getType() {
            return SlotType.COSMETIC;
        }
    };
    
    private static final record Attachment(
            String name,
            ModelAttachment modelAttachment,
            Slot slot
            ) {};
    
    public AttachmentsRegistry() {
        if (instance == null) {
            instance = this;
        } else  {
            attachmentsRegistry.clear();
            instance = this;
        }
    }
    
    public static void finalizeRegister(PlayerReadyEvent event) {
        Ref<EntityStore> ref = event.getPlayerRef();
        Store<EntityStore> store;
        ModelComponent modelComponent;
        PlayerSkinComponent playerSkincomponent;
        CosmeticRegistry cosmeticRegistry = CosmeticsModule.get().getRegistry();
        
        store = ref.getStore();
        modelComponent = store.getComponent(ref, ModelComponent.getComponentType());
        playerSkincomponent = store.getComponent(ref, PlayerSkinComponent.getComponentType());
        
        PlayerSkin playerSkinComponent = playerSkincomponent.getPlayerSkin();
        PlayerSkin playerSkin = playerSkinComponent.clone();
        
        Model playerModel = store.getComponent(ref, ModelComponent.getComponentType()).getModel();
        
        String modelName = event.getPlayer().getDisplayName();
        
        List<ModelAttachment> list = ListUtil.mutable(Arrays.asList(playerModel.getAttachments()));
        
        String gradientId = playerSkin.bodyCharacteristic.split("\\.")[1];
        
        String[] bodyCharacteristicParts = playerSkin.bodyCharacteristic.split("\\.");
        var bodyCharacteristic = cosmeticRegistry.getBodyCharacteristics().get(bodyCharacteristicParts[0]);
        if (bodyCharacteristic != null) {
            list.add(ModelUtils.resolveAttachment(bodyCharacteristic, bodyCharacteristicParts, gradientId));
        }
        
        if (playerSkin.underwear != null) {
            String[] underwearParts = playerSkin.underwear.split("\\.");
            var underwear = cosmeticRegistry.getUnderwear().get(underwearParts[0]);
            if (underwear != null) {
                list.add(ModelUtils.resolveAttachment(underwear, underwearParts, gradientId));
            }
        }
        
        if (playerSkin.eyebrows != null) {
            String[] eyebrowsParts = playerSkin.eyebrows.split("\\.");
            var eyebrows = cosmeticRegistry.getEyebrows().get(eyebrowsParts[0]);
            if (eyebrows != null) {
                list.add(ModelUtils.resolveAttachment(eyebrows, eyebrowsParts, gradientId));
            }
        }
        
        if (playerSkin.eyes != null) {
            String[] eyesParts = playerSkin.eyes.split("\\.");
            var eyes = cosmeticRegistry.getEyes().get(eyesParts[0]);
            if (eyes != null) {
                list.add(ModelUtils.resolveAttachment(eyes, eyesParts, gradientId));
            }
        }
        
        if (playerSkin.ears != null) {
            String[] earsParts = playerSkin.ears.split("\\.");
            var ears = cosmeticRegistry.getEars().get(earsParts[0]);
            if (ears != null) {
                list.add(ModelUtils.resolveAttachment(ears, earsParts, playerSkin.bodyCharacteristic.split("\\.")[1]));
            }
        }
        
        if (playerSkin.face != null) {
            String[] faceParts = playerSkin.face.split("\\.");
            var face = cosmeticRegistry.getFaces().get(faceParts[0]);
            if (face != null) {
                list.add(ModelUtils.resolveAttachment(face, faceParts, playerSkin.bodyCharacteristic.split("\\.")[1]));
            }
        }
        
        if (playerSkin.mouth != null) {
            String[] mouthsParts = playerSkin.mouth.split("\\.");
            var mouths = cosmeticRegistry.getMouths().get(mouthsParts[0]);
            if (mouths != null) {
                list.add(ModelUtils.resolveAttachment(mouths, mouthsParts, gradientId));
            }
        }
        
        if (playerSkin.facialHair != null) {
            String[] facialHairsParts = playerSkin.facialHair.split("\\.");
            var facialHairs = cosmeticRegistry.getFacialHairs().get(facialHairsParts[0]);
            if (facialHairs != null) {
                list.add(ModelUtils.resolveAttachment(facialHairs, facialHairsParts, gradientId));
            }
        }
        
        if (playerSkin.pants != null) {
            String[] pantsParts = playerSkin.pants.split("\\.");
            var pants = cosmeticRegistry.getPants().get(pantsParts[0]);
            if (pants != null) {
                list.add(ModelUtils.resolveAttachment(pants, pantsParts, gradientId));
            }
        }
        
        if (playerSkin.overpants != null) {
            String[] overpantsParts = playerSkin.overpants.split("\\.");
            var overpants = cosmeticRegistry.getOverpants().get(overpantsParts[0]);
            if (overpants != null) {
                list.add(ModelUtils.resolveAttachment(overpants, overpantsParts, gradientId));
            }
        }
        
        if (playerSkin.undertop != null) {
            String[] undertopsParts = playerSkin.undertop.split("\\.");
            var undertops = cosmeticRegistry.getUndertops().get(undertopsParts[0]);
            if (undertops != null) {
                list.add(ModelUtils.resolveAttachment(undertops, undertopsParts, gradientId));
            }
        }
        
        if (playerSkin.overtop != null) {
            String[] overtopsParts = playerSkin.overtop.split("\\.");
            var overtops = cosmeticRegistry.getOvertops().get(overtopsParts[0]);
            if (overtops != null) {
                list.add(ModelUtils.resolveAttachment(overtops, overtopsParts, gradientId));
            }
        }
        
        if (playerSkin.haircut != null) {
            String[] haircutsParts = playerSkin.haircut.split("\\.");
            var haircuts = cosmeticRegistry.getHaircuts().get(haircutsParts[0]);
            if (haircuts != null) {
                list.add(ModelUtils.resolveAttachment(haircuts, haircutsParts, gradientId));
            }
        }
        
        if (playerSkin.shoes != null) {
            String[] shoesParts = playerSkin.shoes.split("\\.");
            var shoes = cosmeticRegistry.getShoes().get(shoesParts[0]);
            if (shoes != null) {
                list.add(ModelUtils.resolveAttachment(shoes, shoesParts, gradientId));
            }
        }
        
        if (playerSkin.headAccessory != null) {
            String[] headAccessoriesParts = playerSkin.headAccessory.split("\\.");
            var headAccessories = cosmeticRegistry.getHeadAccessories().get(headAccessoriesParts[0]);
            if (headAccessories != null) {
                list.add(ModelUtils.resolveAttachment(headAccessories, headAccessoriesParts, gradientId));
            }
        }
        
        if (playerSkin.faceAccessory != null) {
            String[] faceAccessoriesParts = playerSkin.faceAccessory.split("\\.");
            var faceAccessories = cosmeticRegistry.getFaceAccessories().get(faceAccessoriesParts[0]);
            if (faceAccessories != null) {
                list.add(ModelUtils.resolveAttachment(faceAccessories, faceAccessoriesParts, gradientId));
            }
        }
        
        if (playerSkin.earAccessory != null) {
            String[] earAccessoriesParts = playerSkin.earAccessory.split("\\.");
            var earAccessories = cosmeticRegistry.getEarAccessories().get(earAccessoriesParts[0]);
            if (earAccessories != null) {
                list.add(ModelUtils.resolveAttachment(earAccessories, earAccessoriesParts, gradientId));
            }
        }
        
        if (playerSkin.gloves != null) {
            String[] glovesParts = playerSkin.gloves.split("\\.");
            var gloves = cosmeticRegistry.getGloves().get(glovesParts[0]);
            if (gloves != null) {
                list.add(ModelUtils.resolveAttachment(gloves, glovesParts, gradientId));
            }
        }
        
        if (playerSkin.cape != null) {
            String[] capesParts = playerSkin.cape.split("\\.");
            var capes = cosmeticRegistry.getCapes().get(capesParts[0]);
            if (capes != null) {
                list.add(ModelUtils.resolveAttachment(capes, capesParts, gradientId));
            }
        }
        
        if (playerSkin.skinFeature != null) {
            String[] skinFeaturesParts = playerSkin.skinFeature.split("\\.");
            var skinFeatures = cosmeticRegistry.getSkinFeatures().get(skinFeaturesParts[0]);
            if (skinFeatures != null) {
                list.add(ModelUtils.resolveAttachment(skinFeatures, skinFeaturesParts, gradientId));
            }
        }
        
        list.addAll(attachmentsRegistry.values().stream()
                .map(Attachment::modelAttachment)
                .toList()
        );
        
        Model newModel = new Model(
                event.getPlayer().getDisplayName() + "CustomModel",
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
        
        
        
    }
    
    public static void register(String name, Slot slot) {
        attachmentsRegistry.put(
                name,
                new Attachment(
                        name,
                        new ModelAttachment(
                                String.format("Resources/Cosmetics/%s/%s/%s.blockymodel",  slot, name, name),
                                String.format("Resources/Cosmetics/%s/%s/%s.png",  slot, name, name),
                                "",
                                "",
                                1
                        ),
                        slot
                )
        );
    }
    
}
