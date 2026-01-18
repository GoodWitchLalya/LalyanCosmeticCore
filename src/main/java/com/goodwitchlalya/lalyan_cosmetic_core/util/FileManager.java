package com.goodwitchlalya.lalyan_cosmetic_core.util;

import com.goodwitchlalya.lalyan_cosmetic_core.CosmeticCore;
import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.server.core.asset.AssetModule;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class FileManager {
    private static FileManager instance;
    
    public static FileManager getInstance() {
        return instance;
    }
    
    public FileManager() {
        instance = this;
    }
    
    public static String wakeUp() {
        return assetsReload();
    }
    
    public static String assetsReload() {
        AttachmentsRegistry.get().clear();
        
        List<String> r = new ArrayList<>();
        String result;
        
        AssetModule.get().getAssetPacks().forEach(assetPack -> {
            if (assetPack.getName().contains("Hytale:") || assetPack.getName().contains("GoodWitchLalya:LalyanCosmeticCore"))
                return;
            
            if (assetPack.getFileSystem() == null) return;
            
            String s = String.format("Loading asset pack: %s path: (%s)", assetPack.getName(), assetPack.getPackLocation());
            CosmeticCore.log(s);
            r.add(s + "\n");
            
            loadAssets(assetPack, "Common/Resources/Characters", AttachmentsRegistry.CharacterSlot.class, r);
            loadAssets(assetPack, "Common/Resources/Cosmetics", AttachmentsRegistry.CosmeticSlot.class, r);
        });
        
        result = String.join("\n", r);
        return result;
    }
    
    private static <T extends Enum<T> & AttachmentsRegistry.Slot> void loadAssets(AssetPack assetPack, String path, Class<T> enumType, List<String> r) {
        Path folderPath = assetPack.getFileSystem().getPath(path);
        if (!Files.exists(folderPath)) return;
        
        try {
            Files.list(folderPath).filter(Files::isDirectory).forEach(folder -> {
                String folderName = folder.getFileName().toString();
                
                try {
                    T slot = Enum.valueOf(enumType, folderName);
                    r.add(String.format("The folder %s is valid for %s.%s", folderName, enumType.getSimpleName(), slot.name()));
                    
                    try (Stream<Path> itemFolders = Files.list(folder)) {
                        itemFolders.filter(Files::isDirectory).forEach(itemFolder -> {
                            String itemName = itemFolder.getFileName().toString();
                            Path jsonFile = itemFolder.resolve(itemName + ".json");
                            
                            if (Files.exists(jsonFile) && Files.isRegularFile(jsonFile)) {
                                r.add(String.format("Found item '%s' for slot %s", itemName, slot.name()));
                                
                                try {
                                    String jsonContent = new String(Files.readAllBytes(jsonFile));
                                    AttachmentsRegistry.AttachmentData attachmentData = CosmeticCore.GSON.fromJson(jsonContent, AttachmentsRegistry.AttachmentData.class);
                                    attachmentData.slot = slot;
                                    
                                    AttachmentsRegistry.get().register(itemName, attachmentData);
                                } catch (Exception e) {
                                    r.add(String.format("Error reading or parsing JSON for item '%s' in slot %s: %s", itemName, slot.name(), e.getMessage()));
                                }
                            } else {
                                try (Stream<Path> content = Files.list(itemFolder)) {
                                    if (content.findAny().isPresent()) {
                                        AttachmentsRegistry.get().register(itemName, slot);
                                    } else {
                                        r.add(String.format("Folder '%s' in slot %s is empty. Ignoring.", itemName, slot.name()));
                                    }
                                } catch (IOException e) {
                                    r.add(String.format("Error checking folder '%s' in slot %s: %s", itemName, slot.name(), e.getMessage()));
                                }
                            }
                        });
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                } catch (IllegalArgumentException e) {
                    r.add(String.format("The folder %s isn't valid", folderName));
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
}
