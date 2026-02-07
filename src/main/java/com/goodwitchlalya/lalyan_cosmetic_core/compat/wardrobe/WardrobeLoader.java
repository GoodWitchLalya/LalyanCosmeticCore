package com.goodwitchlalya.lalyan_cosmetic_core.compat.wardrobe;

import com.goodwitchlalya.lalyan_cosmetic_core.CosmeticCore;
import com.goodwitchlalya.lalyan_cosmetic_core.util.AttachmentsRegistry;
import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.server.core.asset.AssetModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class WardrobeLoader {
    
    private static WardrobeLoader instance;
    
    public static WardrobeLoader getInstance() {
        return instance;
    }
    
    public WardrobeLoader() {
        instance = this;
    }
    
    /**
     * Alias method to trigger the reload process.
     */
    public static void wakeUp() {
         assetsReload();
    }
    
    public static void assetsReload() {
        // Iterate through all asset packs currently loaded by the server
        AssetModule.get().getAssetPacks().forEach(assetPack -> {
            // Filter out base Hytale packs and the plugin itself to avoid conflicts or recursion
            if (assetPack.getName().contains("Hytale:") || assetPack.getName().contains("GoodWitchLalya:LalyanCosmeticCore")) {
                return;
            }
            
            // Skip packs without a valid file system or path
            if (assetPack.getFileSystem() == null) {
                if (assetPack.getPackLocation() == null) return;
            }
            
            String s = String.format("Loading asset pack: %s path: (%s)", assetPack.getName(), assetPack.getPackLocation());
            CosmeticCore.log(s);
            
            loadWardrobeSlots(assetPack);
            loadWardrobeCosmetics(assetPack);
        });
    }
    
    private static void loadWardrobeCosmetics(AssetPack assetPack) {
        String path = "Server/Wardrobe/Cosmetics";
        
        boolean inDev = false;
        if (assetPack.getFileSystem() == null) {
            if (assetPack.getPackLocation() == null) return;
            else inDev = true;
        }
        
        Path folderPath;
        if (inDev) folderPath = assetPack.getPackLocation().resolve(path);
        else folderPath = assetPack.getFileSystem().getPath(path);
        
        if (!Files.exists(folderPath)) return;
        
        try (Stream<Path> folders = Files.list(folderPath)) {
            folders.filter(Files::isDirectory).forEach(folder -> {
                try (Stream<Path> itemFiles = Files.list(folder)) {
                    itemFiles.filter(p -> p.getFileName().toString().endsWith(".json")).forEach(jsonFile -> {
                        try {
                            String jsonContent = new String(Files.readAllBytes(jsonFile));
                            RawJsonReader reader = new RawJsonReader(jsonContent.toCharArray());
                            WardrobeCompatLayer wardrobeData = WardrobeCompatLayer.CODEC.decodeJson(reader, new ExtraInfo());
                            
                            AttachmentsRegistry.Slot slot = AttachmentsRegistry.get().slotFromName(wardrobeData.getSlot());
                            if (slot == null) {
                                CosmeticCore.log("Unknown slot: " + wardrobeData.getSlot());
                                return;
                            }
                            
                            String itemName = jsonFile.getFileName().toString().replace(".json", "");
                            
                            AttachmentsRegistry.AttachmentData attachmentData = new AttachmentsRegistry.AttachmentData();
                            attachmentData.slot = slot;
                            attachmentData.icon = wardrobeData.getProperties().icon();
                            
                            if (wardrobeData.getAppearance() instanceof WardrobeCompatLayer.ModelAppearance modelAppearance) {
                                attachmentData.model = modelAppearance.getModel(null);
                                WardrobeCompatLayer.TextureConfig textureConfig = modelAppearance.getTextureConfig(null);
                                if (textureConfig instanceof WardrobeCompatLayer.StaticTextureConfig staticTextureConfig) {
                                    attachmentData.texture = staticTextureConfig.getTexture(null);
                                } else if (textureConfig instanceof WardrobeCompatLayer.GradientTextureConfig gradientTextureConfig) {
                                    attachmentData.texture = gradientTextureConfig.getTexture(null);
                                    if (attachmentData.alternatives == null) attachmentData.alternatives = new AttachmentsRegistry.Alternative();
                                    attachmentData.alternatives.gradientSet = gradientTextureConfig.getGradientSet();
                                } else if (textureConfig instanceof WardrobeCompatLayer.VariantTextureConfig variantTextureConfig) {
                                    String var = variantTextureConfig.getVariants().keySet().stream().findFirst().get();
                                    attachmentData.texture = variantTextureConfig.getTexture(var);
                                    if (attachmentData.alternatives == null) attachmentData.alternatives = new AttachmentsRegistry.Alternative();
                                    attachmentData.alternatives.variants = variantTextureConfig.getFormattedVariants();
                                }
                            } else if (wardrobeData.getAppearance() instanceof WardrobeCompatLayer.VariantAppearance variantAppearance) {
                                Map<String, AttachmentsRegistry.Variant> variants = new HashMap<>();
                                variantAppearance.getVariants().forEach((key, entry) -> {
                                    String texture = null;
                                    if (entry.getTextureConfig() instanceof WardrobeCompatLayer.StaticTextureConfig staticTextureConfig) {
                                        texture = staticTextureConfig.getTexture(null);
                                    } else if (entry.getTextureConfig() instanceof WardrobeCompatLayer.GradientTextureConfig gradientTextureConfig) {
                                        texture = gradientTextureConfig.getTexture(null);
                                    }
                                    variants.put(key, new AttachmentsRegistry.Variant(texture, entry.getIcon()));
                                });
                                if (attachmentData.alternatives == null) attachmentData.alternatives = new AttachmentsRegistry.Alternative();
                                attachmentData.alternatives.variants = variants;
                                
                                if (!variants.isEmpty()) {
                                    Map.Entry<String, WardrobeCompatLayer.VariantAppearance.Entry> firstEntry = variantAppearance.getVariants().entrySet().iterator().next();
                                    attachmentData.model = firstEntry.getValue().getModel();
                                }
                            }
                            
                            AttachmentsRegistry.get().registerJson(assetPack.getName() + "#" + itemName, attachmentData);
                            CosmeticCore.log("Loaded Wardrobe cosmetic: " + itemName);
                            
                        } catch (Exception e) {
                            CosmeticCore.log("Error loading Wardrobe cosmetic " + jsonFile + ": " + e.getMessage());
                            e.printStackTrace();
                        }
                    });
                } catch (IOException e) {
                    CosmeticCore.log("Error listing files in " + folder + ": " + e.getMessage());
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static void loadWardrobeSlots(AssetPack assetPack) {
        String path = "Server/Wardrobe/Slots";
        
        boolean inDev = false;
        if (assetPack.getFileSystem() == null) {
            if (assetPack.getPackLocation() == null) return;
            else inDev = true;
        }
        
        Path folderPath;
        if (inDev) folderPath = assetPack.getPackLocation().resolve(path);
        else folderPath = assetPack.getFileSystem().getPath(path);
        
        if (!Files.exists(folderPath)) return;
        
        try (Stream<Path> slotFiles = Files.list(folderPath)) {
            slotFiles.filter(p -> p.getFileName().toString().endsWith(".json")).forEach(jsonFile -> {
                try {
                    String jsonContent = new String(Files.readAllBytes(jsonFile));
                    RawJsonReader reader = new RawJsonReader(jsonContent.toCharArray());
                    WardrobeCompatLayer.SlotData slotData = WardrobeCompatLayer.SlotData.CODEC.decodeJson(reader, new ExtraInfo());
                    
                    AttachmentsRegistry.Slot slot = new AttachmentsRegistry.Slot();
                    slot.name = jsonFile.getFileName().toString().replace(".json", "");
                    slot.icon = slotData.getProperties().icon();
                    slot.selectedIcon = slotData.getSelectedIcon();
                    slot.tlcName = slotData.getCategory(); // Default to "All" or handle categories if Wardrobe has them
                    slot.canVanish = true;
                    
                    AttachmentsRegistry.get().registerSlot(slot);
                    CosmeticCore.log("Loaded Wardrobe slot: " + slot.name);
                    
                } catch (Exception e) {
                    CosmeticCore.log("Error loading Wardrobe slot " + jsonFile + ": " + e.getMessage());
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
}
