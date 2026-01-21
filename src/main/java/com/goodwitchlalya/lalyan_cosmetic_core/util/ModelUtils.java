package com.goodwitchlalya.lalyan_cosmetic_core.util;

import com.hypixel.hytale.server.core.asset.type.model.config.ModelAttachment;
import com.hypixel.hytale.server.core.cosmetics.PlayerSkinPart;
import com.hypixel.hytale.server.core.cosmetics.PlayerSkinPartTexture;

/**
 * Utility class for handling Hytale player model attachments and resolving skin parts.
 * This class provides methods to convert PlayerSkinPart objects into ModelAttachment objects,
 * and to resolve complex skin part configurations including variants and custom textures/gradients.
 */
public class ModelUtils {
    /**
     * Creates a ModelAttachment from a given PlayerSkinPart and a gradient ID.
     * This is typically used for base skin parts without specific variants or custom textures.
     *
     * @param part The PlayerSkinPart to convert.
     * @param gradientId The ID of the gradient to apply to the model's greyscale texture.
     * @return A new ModelAttachment instance.
     */
    public static ModelAttachment fromPlayerSkinPart(PlayerSkinPart part, String gradientId) {
        return new ModelAttachment(
                part.getModel(),          // The model asset path.
                part.getGreyscaleTexture(), // The base greyscale texture asset path.
                part.getGradientSet(),    // The gradient set to use.
                gradientId,               // The specific gradient ID from the set.
                1                         // The layer of the attachment.
        );
    }
    
    /**
     * Resolves a complex PlayerSkinPart into a ModelAttachment, considering variants,
     * custom textures, and gradient IDs based on an array of parts from a player skin string.
     * The 'parts' array typically comes from splitting a string like "partName.variantName.textureName.gradientId".
     *
     * @param part The base PlayerSkinPart definition.
     * @param parts An array of strings representing the components of the skin part (e.g., ["bodyCharacteristic", "variant", "texture", "gradient"]).
     * @param defaultGradientId The default gradient ID to use if not specified in the parts array.
     * @return A new ModelAttachment instance configured with the resolved model, texture, and gradient.
     */
    public static ModelAttachment resolveAttachment(PlayerSkinPart part, String[] parts, String defaultGradientId) {
        // Extract potential variant/texture/gradient identifiers from the parts array.
        // p1 could be a variant, texture, or gradient.
        // p2 could be a texture or gradient.
        String p1 = parts.length > 1 ? parts[1] : null;
        String p2 = parts.length > 2 ? parts[2] : null;
        
        // --- Scenario 1: p1 is a variant ---
        // Check if p1 matches a known variant for this skin part.
        if (p1 != null && part.getVariants() != null && part.getVariants().containsKey(p1)) {
            PlayerSkinPart.Variant variant = part.getVariants().get(p1);
            
            // Check if p2 is a specific texture for this variant.
            if (p2 != null && variant.getTextures() != null && variant.getTextures().containsKey(p2)) {
                PlayerSkinPartTexture texture = variant.getTextures().get(p2);
                return new ModelAttachment(
                        variant.getModel(),          // Use the variant's model.
                        texture.getTexture(),        // Use the specific variant texture.
                        part.getGradientSet(),       // Use the base part's gradient set.
                        defaultGradientId,           // Use the default gradient ID.
                        1
                );
            }
            
            // If p2 is not a texture, it's treated as a gradient ID for the variant's default texture.
            return new ModelAttachment(
                    variant.getModel(),          // Use the variant's model.
                    variant.getGreyscaleTexture(), // Use the variant's default greyscale texture.
                    part.getGradientSet(),       // Use the base part's gradient set.
                    p2 != null ? p2 : defaultGradientId, // Use p2 as gradient, or default.
                    1
            );
        }
        
        // --- Scenario 2: p2 is a variant (less common, but handled) ---
        // Check if p2 matches a known variant for this skin part.
        if (p2 != null && part.getVariants() != null && part.getVariants().containsKey(p2)) {
            PlayerSkinPart.Variant variant = part.getVariants().get(p2);
            
            // Check if p1 is a specific texture for this variant.
            if (p1 != null && variant.getTextures() != null && variant.getTextures().containsKey(p1)) {
                PlayerSkinPartTexture texture = variant.getTextures().get(p1);
                return new ModelAttachment(
                        variant.getModel(),          // Use the variant's model.
                        texture.getTexture(),        // Use the specific variant texture.
                        part.getGradientSet(),       // Use the base part's gradient set.
                        defaultGradientId,           // Use the default gradient ID.
                        1
                );
            }
            
            // If p1 is not a texture, it's treated as a gradient ID for the variant's default texture.
            return new ModelAttachment(
                    variant.getModel(),          // Use the variant's model.
                    variant.getGreyscaleTexture(), // Use the variant's default greyscale texture.
                    part.getGradientSet(),       // Use the base part's gradient set.
                    p1,                          // Use p1 as gradient.
                    1
            );
        }
        
        // --- Scenario 3: No explicit variant, p1 is a custom texture for the base part ---
        // Check if p1 matches a known custom texture for the base skin part.
        if (p1 != null && part.getTextures() != null && part.getTextures().containsKey(p1)) {
            PlayerSkinPartTexture texture = part.getTextures().get(p1);
            return new ModelAttachment(
                    part.getModel(),          // Use the base part's model.
                    texture.getTexture(),     // Use the specific custom texture.
                    part.getGradientSet(),    // Use the base part's gradient set.
                    p2 != null ? p2 : defaultGradientId, // Use p2 as gradient, or default.
                    1
            );
        }
        
        // --- Scenario 4: No explicit variant, p2 is a custom texture for the base part (implies p1 is gradient) ---
        // Check if p2 matches a known custom texture for the base skin part.
        if (p2 != null && part.getTextures() != null && part.getTextures().containsKey(p2)) {
            PlayerSkinPartTexture texture = part.getTextures().get(p2);
            return new ModelAttachment(
                    part.getModel(),          // Use the base part's model.
                    texture.getTexture(),     // Use the specific custom texture.
                    part.getGradientSet(),    // Use the base part's gradient set.
                    p1,                       // Use p1 as gradient.
                    1
            );
        }
        
        // --- Scenario 5: No variants or custom textures found, use base part with resolved gradient ---
        // If none of the above conditions are met, fall back to the base PlayerSkinPart
        // and apply p1 as the gradient if present, otherwise use the default gradient.
        return fromPlayerSkinPart(part, p1 != null ? p1 : defaultGradientId);
    }
}