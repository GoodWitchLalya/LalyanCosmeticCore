package com.goodwitchlalya.lalyan_cosmetic_core.util;

import com.goodwitchlalya.lalyan_cosmetic_core.CosmeticCore;
import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.protocol.Direction;
import com.hypixel.hytale.protocol.Position;
import com.hypixel.hytale.server.core.asset.AssetModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Singleton class responsible for scanning, parsing, and loading assets
 * (both Cosmetics and Character parts) from the server's loaded AssetPacks.
 */
public class FileManager {
    private static FileManager instance;
    
    public static FileManager getInstance() {
        return instance;
    }
    
    public FileManager() {
        instance = this;
    }
    
    /**
     * Alias method to trigger the reload process.
     */
    public static String wakeUp() {
        return assetsReload();
    }
    
    /**
     * Reloads all cosmetics by scanning available AssetPacks.
     * 1. Clears the current registry.
     * 2. Iterates through valid AssetPacks (ignoring vanilla Hytale and the Core itself).
     * 3. Loads Character slots (e.g., Beards, Eyes) and Cosmetic slots (e.g., Hats, Capes).
     *
     * @return A log string containing details about the loaded assets and errors.
     */
    public static String assetsReload() {
        // Clears the registry to prevent duplicates during reload
        AttachmentsRegistry.get().clear();
        
        List<String> r = new ArrayList<>();
        String result;
        
        AssetPack base = AssetModule.get().getAssetPack("GoodWitchLalya:Lalyan Cosmetic Core");
        
        loadTopLevelCategories(base);
        loadSlots(base);
        
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
            r.add(s + "\n");
            
            loadTopLevelCategories(assetPack);
            loadSlots(assetPack);
            
            loadCosmetics(assetPack, "Common/Resources/Characters", r);
            loadCosmetics(assetPack, "Common/Resources/Cosmetics", r);
            loadCosmetics(assetPack, "Common/Resources", r);
        });
        
        AttachmentsRegistry.TopLevelCategory all = new AttachmentsRegistry.TopLevelCategory();
        all.name = "All";
        AttachmentsRegistry.get().registerTLC(all);
        
        AttachmentsRegistry.Slot allSlot = new AttachmentsRegistry.Slot();
        
        allSlot.name = "All";
        allSlot.icon = "UI/Custom/Common/Categories/Top/All.png";
        allSlot.selectedIcon = "UI/Custom/Common/Categories/Top/Selected/All.png";
        allSlot.tlcName = "All";
        
        AttachmentsRegistry.SlotCameraProperties cam = new AttachmentsRegistry.SlotCameraProperties();
        cam.distance = 1;
        cam.positionOffset = new Position(0, 0, 0);
        cam.lookAtBack = false;
        cam.rotation = new Direction(0, 0, 0);
        allSlot.camera = cam;
        
        AttachmentsRegistry.get().registerSlot(allSlot);
        
        result = String.join("\n", r);
        return result;
    }
    
    /**
     * Generic method to load assets from a specific directory.
     * Supports both CosmeticSlot and CharacterSlot via Generics.
     *
     * @param assetPack The asset pack being scanned.
     * @param path The base path to scan (e.g., "Common/Resources/Cosmetics").
     * @param r List to accumulate log messages.
     */
    private static void loadCosmetics(AssetPack assetPack, String path, List<String> r) {
        boolean inDev = false;
        if (assetPack.getFileSystem() == null) {
            if (assetPack.getPackLocation() == null) return;
            else inDev = true;
        }
        
        Path folderPath;
        if (inDev) folderPath = assetPack.getPackLocation().resolve(path);
        else folderPath = assetPack.getFileSystem().getPath(path);
        
        // Exit if the target folder doesn't exist in this asset pack
        if (!Files.exists(folderPath)) return;
        
        try {
            // 1. Scan category/slot folders (e.g., "Head", "Capes")
            Files.list(folderPath).filter(Files::isDirectory).forEach(folder -> {
                String folderName = folder.getFileName().toString();
                
                if(folderName.contains("Character") || folderName.contains("Cosmetic")) return;
                
                try {
                    // Attempt to convert the folder name into a valid Enum constant
                    AttachmentsRegistry.Slot slot = AttachmentsRegistry.get().slotFromName(folderName);
                    
                    // 2. Scan for specific items inside the slot folder (e.g., "Alien_Antenna")
                    try (Stream<Path> itemFolders = Files.list(folder)) {
                        itemFolders.filter(Files::isDirectory).forEach(itemFolder -> {
                            String itemName = itemFolder.getFileName().toString();
                            
                            // Check for a custom JSON configuration file
                            Path jsonFile = itemFolder.resolve(itemName + ".json");
                            
                            // --- CASE A: JSON Configuration Found ---
                            if (Files.exists(jsonFile) && Files.isRegularFile(jsonFile)) {
                                r.add(String.format("Found item '%s' for slot %s", itemName, slot.name));
                                
                                try {
                                    // Read and deserialize the JSON into AttachmentData
                                    String jsonContent = new String(Files.readAllBytes(jsonFile));
                                    
                                    // Parse JSON using Codec system
                                    RawJsonReader reader = new RawJsonReader(jsonContent.toCharArray());
                                    AttachmentsRegistry.AttachmentData attachmentData = AttachmentsRegistry.AttachmentData.CODEC.decodeJson(reader, new ExtraInfo());
                                    
                                    // Inject the slot type (inferred from the folder structure)
                                    attachmentData.slot = slot;
                                    
                                    // Register using the detailed JSON data
                                    
                                    // If all required files are present, register the attachment
                                    AttachmentsRegistry.get().registerJson(assetPack.getName() + "#" + itemName, attachmentData);
                                } catch (Exception e) {
                                    r.add(String.format("Error reading or parsing JSON for item '%s' in slot %s: %s", itemName, slot.name, e.getMessage()));
                                }
                            }
                            // --- CASE B: Standard Loading (No JSON) ---
                            else {
                                try (Stream<Path> content = Files.list(itemFolder)) {
                                    if (content.findAny().isPresent()) {
                                        boolean isColoured = false;
                                        
                                        // Check if the Item is coloured
                                        Pattern colourPattern = Pattern.compile("(.*)_Colors_(.*)");
                                        Matcher colourMatcher = colourPattern.matcher(itemFolder.getFileName().toString());
                                        String gradientSet = "";
                                        if (colourMatcher.find()) {
                                            if (!colourMatcher.group(1).isEmpty() && !colourMatcher.group(2).isEmpty()) {
                                                
                                                itemName = colourMatcher.group(1);
                                                gradientSet = colourMatcher.group(2);
                                                
                                                isColoured = true;
                                                
                                            } else {
                                                 if (colourMatcher.group(1).isEmpty()) {
                                                    r.add(String.format("item: %s, no name found", itemName));
                                                 }
                                                 if (colourMatcher.group(2).isEmpty()) {
                                                    r.add(String.format("item: %s, no gradient found", itemName));
                                                 }
                                            }
                                        }
                                        
                                        // Verify that the folder is not empty (contains at least one file, e.g., the model or texture)
                                        // Register using default naming conventions
                                        
                                        // Check if the 'Icon' subfolder exists
                                        boolean isIconFolderThere = false;
                                        // Check if the icon image exists within the 'Icon' subfolder
                                        boolean isIconThere = false;
                                        // Check if the main texture file exists
                                        boolean isTextureThere = false;
                                        // Check if the blocky model file exists
                                        boolean isModelThere = false;
                                        
                                        // Check if the 'Icon' subfolder exists
                                        isIconFolderThere = Files.exists(itemFolder.resolve("Icon"));
                                        if  (!isIconFolderThere) { r.add("No Icon folder found"); return;}
                                        // Check if the icon image exists within the 'Icon' subfolder
                                        isIconThere = Files.exists(itemFolder.resolve("Icon").resolve(itemName + ".png"));
                                        if  (!isIconThere) { r.add("No icon image found"); return;}
                                        // Check if the blocky model file exists
                                        isModelThere = Files.exists(itemFolder.resolve(itemName + ".blockymodel"));
                                        if  (!isModelThere) { r.add("No model found"); return;}
                                        // Check if the main texture file exists
                                        isTextureThere = Files.exists(itemFolder.resolve(itemName + ".png"));
                                        if  (!isTextureThere) { r.add("No texture found"); return;}
                                        
                                        // If all required files are present, register the attachment
                                        Map<String, AttachmentsRegistry.Variant> variants = new HashMap<>();
                                        
                                        if (isColoured) {
                                        
                                        } else {
                                            try (Stream<Path> itemFolderFiles = Files.list(itemFolder)) {
                                                String finalItemName = itemName;
                                                itemFolderFiles.filter(Files::isRegularFile).filter((item) -> {// Search for suitable variants
                                                    if (item.getFileName().toString().matches(String.format("%s_Variant_.*\\.png", finalItemName))) return true;
                                                    else return false;
                                                }).forEach(variant -> {// For each suitable variant
                                                    // Verify if the variant is valid and get the variant name
                                                    String variantFileName = variant.getFileName().toString();
                                                    String variantName = "";
                                                    Pattern pattern = Pattern.compile("_Variant_(.*)\\.png");
                                                    Matcher matcher = pattern.matcher(variantFileName);
                                                    if (matcher.find()) {
                                                        variantName = matcher.group(1);
                                                    }
                                                    r.add(String.format("Found a variant (%s) for %s in [%s]\nChecking for an icon", variantName, finalItemName, variantFileName));
                                                    
                                                    //Getting the variable texture path
                                                    String variantTexturePath = itemFolder.resolve(variantFileName).toString().replace("\\", "/").replaceFirst(".*?(?=Resources)", "");
                                                    r.add(String.format("Variant texture found, path: %s", variantTexturePath));
                                                    
                                                    //Getting the variable icon path
                                                    String variantIconPath = itemFolder.resolve("Icon").resolve(variantFileName).toString().replace("\\", "/").replaceFirst(".*?(?=Resources)", "");
                                                    r.add(String.format("Variant icon found, path: %s", variantIconPath));
                                                    
                                                    variants.put(variantName, new AttachmentsRegistry.Variant(variantTexturePath, variantIconPath));
                                                    r.add("Variant saved!");
                                                });
                                            } catch (Exception e) {
                                                r.add(String.format("Cannot access %s for searching variants", itemFolder.getFileName().toString()));
                                            }
                                        }
                                        
                                        String attachmentPath = itemFolder.toString().replace("\\", "/").replaceFirst(".*?(?=Resources)", "");
                                        
                                        // Register the attachment
                                        if (isColoured) {
                                            AttachmentsRegistry.get().registerJsonLess(assetPack.getName() + "#" + itemName, slot, attachmentPath, gradientSet);
                                        } else if (!variants.isEmpty()) {
                                            AttachmentsRegistry.get().registerJsonLess(assetPack.getName() + "#" + itemName, slot, attachmentPath, new HashMap<>(variants));
                                        } else {
                                            AttachmentsRegistry.get().registerJsonLess(assetPack.getName() + "#" + itemName, slot, attachmentPath);
                                        }
                                        
                                    } else {
                                        // Log if the folder is empty
                                        r.add(String.format("Folder '%s' in slot %s is empty. Ignoring.", itemName, slot.name));
                                    }
                                } catch (IOException e) {
                                    r.add(String.format("Error checking folder '%s' in slot %s: %s", itemName, slot.name, e.getMessage()));
                                }
                            }
                        });
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                } catch (IllegalArgumentException e) {
                    // The folder name does not match any valid slot in the Enum
                    r.add(String.format("The folder %s isn't valid", folderName));
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static void loadTopLevelCategories(AssetPack assetPack) {
        String path = "Common/Resources/CosmeticSlots/TopLevelCategories";
        
        boolean inDev = false;
        if (assetPack.getFileSystem() == null) {
            if (assetPack.getPackLocation() == null) return;
            else inDev = true;
        }
        
        Path folderPath;
        if (inDev) folderPath = assetPack.getPackLocation().resolve(path);
        else folderPath = assetPack.getFileSystem().getPath(path);
        
        if (!Files.exists(folderPath)) return;
        
        try {
            Files.list(folderPath).filter(p -> p.getFileName().toString().endsWith(".json")).forEach(p -> {
                
                try {
                    String jsonContent = new String(Files.readAllBytes(p));
                    
                    // Parse JSON using Codec system
                    RawJsonReader reader = new RawJsonReader(jsonContent.toCharArray());
                    AttachmentsRegistry.TopLevelCategory tlc = AttachmentsRegistry.TopLevelCategory.CODEC.decodeJson(reader, new ExtraInfo());
                    
                    AttachmentsRegistry.get().registerTLC(tlc);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static void loadSlots(AssetPack assetPack) {
        String path = "Common/Resources/CosmeticSlots/Slots";
        
        boolean inDev = false;
        if (assetPack.getFileSystem() == null) {
            if (assetPack.getPackLocation() == null) return;
            else inDev = true;
        }
        
        Path folderPath;
        if (inDev) folderPath = assetPack.getPackLocation().resolve(path);
        else folderPath = assetPack.getFileSystem().getPath(path);
        
        if (!Files.exists(folderPath)) return;
        
        try {
            Files.list(folderPath).filter(p -> p.getFileName().toString().endsWith(".json")).forEach(p -> {
                try {
                    String jsonContent = new String(Files.readAllBytes(p));
                    
                    // Parse JSON using Codec system
                    RawJsonReader reader = new RawJsonReader(jsonContent.toCharArray());
                    AttachmentsRegistry.Slot tlc = AttachmentsRegistry.Slot.CODEC.decodeJson(reader, new ExtraInfo());
                    
                    AttachmentsRegistry.get().registerSlot(tlc);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}