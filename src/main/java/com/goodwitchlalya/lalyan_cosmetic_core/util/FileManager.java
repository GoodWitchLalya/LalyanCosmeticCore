package com.goodwitchlalya.lalyan_cosmetic_core.util;

import com.goodwitchlalya.lalyan_cosmetic_core.CosmeticCore;
import com.goodwitchlalya.lalyan_cosmetic_core.util.AttachmentsRegistry.ModelVariant;
import com.goodwitchlalya.lalyan_cosmetic_core.util.AttachmentsRegistry.ColorVariant;
import com.goodwitchlalya.lalyan_cosmetic_core.util.AttachmentsRegistry.Variant;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hypixel.hytale.assetstore.AssetPack;
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
        
        // Iterate through all asset packs currently loaded by the server
        AssetModule.get().getAssetPacks().forEach(assetPack -> {
            // Filter out base Hytale packs and the plugin itself to avoid conflicts or recursion
            if (assetPack.getName().contains("Hytale:") || assetPack.getName().contains("GoodWitchLalya:LalyanCosmeticCore"))
                return;
            
            // Skip packs without a valid file system or path
            boolean inDev = false;
            if (assetPack.getFileSystem() == null) {
                if (assetPack.getPackLocation() == null) return;
                else inDev = true;
            }
            
            String s = String.format("Loading asset pack: %s path: (%s)", assetPack.getName(), assetPack.getPackLocation());
            CosmeticCore.log(s);
            r.add(s + "\n");
            
            // Load Body Parts (mapped to CharacterSlot enum)
            loadAssets(assetPack, "Common/Resources/Characters", AttachmentsRegistry.CharacterSlot.class, r);
            
            // Load Cosmetics (mapped to CosmeticSlot enum)
            loadAssets(assetPack, "Common/Resources/Cosmetics", AttachmentsRegistry.CosmeticSlot.class, r);
        });
        
        result = String.join("\n", r);
        return result;
    }
    
    /**
     * Generic method to load assets from a specific directory.
     * Supports both CosmeticSlot and CharacterSlot via Generics.
     *
     * @param assetPack The asset pack being scanned.
     * @param path The base path to scan (e.g., "Common/Resources/Cosmetics").
     * @param enumType The Enum class to map folder names to (CosmeticSlot.class or CharacterSlot.class).
     * @param r List to accumulate log messages.
     * @param <T> Generic type ensuring the class is an Enum and implements the Slot interface.
     */
    private static <T extends Enum<T> & AttachmentsRegistry.Slot> void loadAssets(AssetPack assetPack, String path, Class<T> enumType, List<String> r) {
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
                
                try {
                    // Attempt to convert the folder name into a valid Enum constant
                    T slot = Enum.valueOf(enumType, folderName);
                    r.add(String.format("The folder %s is valid for %s.%s", folderName, enumType.getSimpleName(), slot.name()));
                    
                    // 2. Scan for specific items inside the slot folder (e.g., "Alien_Antenna")
                    try (Stream<Path> itemFolders = Files.list(folder)) {
                        itemFolders.filter(Files::isDirectory).forEach(itemFolder -> {
                            String itemName = itemFolder.getFileName().toString();
                            
                            // Check for a custom JSON configuration file
                            Path jsonFile = itemFolder.resolve(itemName + ".json");
                            
                            // --- CASE A: JSON Configuration Found ---
                            if (Files.exists(jsonFile) && Files.isRegularFile(jsonFile)) {
                                r.add(String.format("Found item '%s' for slot %s", itemName, slot.name()));
                                
                                try {
                                    // Read and deserialize the JSON into AttachmentData
                                    String jsonContent = new String(Files.readAllBytes(jsonFile));
                                    
                                    // Custom deserialization logic to handle polymorphic variants
                                    JsonObject jsonObject = JsonParser.parseString(jsonContent).getAsJsonObject();
                                    
                                    String model = jsonObject.has("model") ? jsonObject.get("model").getAsString() : null;
                                    String texture = jsonObject.has("texture") ? jsonObject.get("texture").getAsString() : null;
                                    String icon = jsonObject.has("icon") ? jsonObject.get("icon").getAsString() : null;
                                    String gradientSet = jsonObject.has("gradientSet") ? jsonObject.get("gradientSet").getAsString() : null;
                                    String gradientId = jsonObject.has("gradientId") ? jsonObject.get("gradientId").getAsString() : null;
                                    
                                    Map<String, Variant> variants = new HashMap<>();
                                    if (jsonObject.has("variants")) {
                                        JsonObject variantsObj = jsonObject.getAsJsonObject("variants");
                                        for (Map.Entry<String, JsonElement> entry : variantsObj.entrySet()) {
                                            JsonObject variantObj = entry.getValue().getAsJsonObject();
                                            String type = variantObj.has("type") ? variantObj.get("type").getAsString() : "model";
                                            
                                            if ("color".equals(type)) {
                                                variants.put(entry.getKey(), CosmeticCore.GSON.fromJson(variantObj, ColorVariant.class));
                                            } else {
                                                variants.put(entry.getKey(), CosmeticCore.GSON.fromJson(variantObj, ModelVariant.class));
                                            }
                                        }
                                    }
                                    
                                    List<String> slotOverrides = new ArrayList<>();
                                    if (jsonObject.has("slot_overrides")) {
                                        for (JsonElement e : jsonObject.getAsJsonArray("slot_overrides")) {
                                            slotOverrides.add(e.getAsString());
                                        }
                                    }
                                    
                                    AttachmentsRegistry.AttachmentData attachmentData = new AttachmentsRegistry.AttachmentData(model, texture, icon, gradientSet, gradientId, variants, slotOverrides);
                                    
                                    // Inject the slot type (inferred from the folder structure)
                                    attachmentData.slot = slot;
                                    
                                    // Register using the detailed JSON data
                                    
                                    // If all required files are present, register the attachment
                                    AttachmentsRegistry.get().register(assetPack.getName() + "#" + itemName, attachmentData);
                                } catch (Exception e) {
                                    r.add(String.format("Error reading or parsing JSON for item '%s' in slot %s: %s", itemName, slot.name(), e.getMessage()));
                                }
                            }
                            // --- CASE B: Standard Loading (No JSON) ---
                            else {
                                try (Stream<Path> content = Files.list(itemFolder)) {
                                    // Verify that the folder is not empty (contains at least one file, e.g., the model or texture)
                                    if (content.findAny().isPresent()) {
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
                                        Map<String, Variant> variants = new HashMap<>();
                                        
                                        try (Stream<Path> itemFolderFiles = Files.list(itemFolder)) {
                                            itemFolderFiles.filter(Files::isRegularFile).filter((item) -> {// Search for suitable variants
                                                if (item.getFileName().toString().matches(String.format("%s_Variant_.*\\.png", itemName))) return true;
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
                                                r.add(String.format("Found a variant (%s) for %s in [%s]\nChecking for an icon", variantName, itemName, variantFileName));
                                                
                                                //Getting the variable texture path
                                                String variantTexturePath = itemFolder.resolve(variantFileName).toString().replace("\\", "/").replaceFirst(".*?(?=Resources)", "");
                                                r.add(String.format("Variant texture found, path: %s", variantTexturePath));
                                                
                                                //Getting the variable icon path
                                                String variantIconPath = itemFolder.resolve("Icon").resolve(variantFileName).toString().replace("\\", "/").replaceFirst(".*?(?=Resources)", "");
                                                r.add(String.format("Variant icon found, path: %s", variantIconPath));
                                                
                                                variants.put(variantName, new ModelVariant(variantTexturePath, variantIconPath, null));
                                                r.add("Variant saved!");
                                            });
                                            if (variants.isEmpty()) {
                                                AttachmentsRegistry.get().register(assetPack.getName() + "#" + itemName, slot);
                                            } else {
                                                AttachmentsRegistry.get().register(assetPack.getName() + "#" + itemName, slot, new HashMap<>(variants));
                                            }
                                        } catch (Exception e) {
                                            r.add(String.format("Cannot access %s for searching variants", itemFolder.getFileName().toString()));
                                        }
                                    } else {
                                        // Log if the folder is empty
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
                    // The folder name does not match any valid slot in the Enum
                    r.add(String.format("The folder %s isn't valid", folderName));
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}