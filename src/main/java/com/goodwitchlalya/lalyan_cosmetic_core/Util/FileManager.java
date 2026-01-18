package com.goodwitchlalya.lalyan_cosmetic_core.Util;

import com.goodwitchlalya.lalyan_cosmetic_core.CosmeticCore;
import com.hypixel.hytale.server.core.asset.AssetModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class FileManager {
    private static FileManager instance;
    public static FileManager getInstance() {return instance;}
    
    public FileManager() {
        instance = this;
    }
    public static String wakeUp() {
        return assetsReload();
    }
    
    public static String assetsReload() {
        
        AttachmentsRegistry.clear();
        
        List<String> r = new ArrayList<String>();
        String result = "";
        
        AssetModule.get().getAssetPacks().forEach(assetPack -> {
            if (!((assetPack.getName().contains("Hytale:Hytale")) || (assetPack.getName().contains("GoodWitchLalya:Lalyan Cosmetic Core")))) {
                
                String s = String.format("Loading asset pack: %s path: (%s)", assetPack.getName(), assetPack.getPackLocation());
                CosmeticCore.log(s);
                r.add(s + "\n");
                
                Path path = assetPack.getPackLocation();
                
                try {// FALLO ANCHE PER IL CHARACTERS
                    Files.list(assetPack.getFileSystem().getPath("Common/Resources/Cosmetics")).filter(Files::isDirectory).forEach(folder -> {
                        
                        String folderName = folder.getFileName().toString();
                        
                        try {
                            AttachmentsRegistry.CosmeticSlot slot = AttachmentsRegistry.CosmeticSlot.valueOf(folderName);
                            r.add(String.format("The folder %s is valid for CosmeticSlot.%s", folderName,  slot.name()));
                            
                            /* This is a folder that is valid as a slot folder */
                            
                            try (Stream<Path> cosmeticItems = Files.list(folder)) {
                                
                                cosmeticItems
                                        .filter(Files::isDirectory)
                                        .forEach(itemFolder -> {
                                            
                                            boolean isNotEmpty = false;
                                            try (Stream<Path> content = Files.list(itemFolder)) {
                                                isNotEmpty = content.findAny().isPresent();
                                            } catch (IOException e) {
                                            
                                            }
                                            
                                            if (isNotEmpty) {
                                                
                                                /* The folder has cosmetics inside */
                                                String cosmeticName = itemFolder.getFileName().toString();
                                                
                                                /*
                                                 * Devi controllare che ci siano effettivamente i files corretti dentro la directory
                                                 */
                                                
                                                AttachmentsRegistry.register(cosmeticName, slot);
                                                
                                            }
                                        
                                        });
                                
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                            
                        } catch (IllegalArgumentException e) {
                            r.add(String.format("The folder %s isn't valid",  folderName));
                        }
                    });
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                
                
            }
        });
        
        result = String.join("\n", r);
        return result;
    }
    
}
