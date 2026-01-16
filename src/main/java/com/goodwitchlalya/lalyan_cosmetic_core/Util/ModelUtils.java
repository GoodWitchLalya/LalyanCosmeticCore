package com.goodwitchlalya.lalyan_cosmetic_core.Util;

import com.hypixel.hytale.server.core.asset.type.model.config.ModelAttachment;
import com.hypixel.hytale.server.core.cosmetics.PlayerSkinPart;
import com.hypixel.hytale.server.core.cosmetics.PlayerSkinPartTexture;

public class ModelUtils {
    public static ModelAttachment fromPlayerSkinPart(PlayerSkinPart part, String gradientId) {
        return new ModelAttachment(
                part.getModel(),
                part.getGreyscaleTexture(),
                part.getGradientSet(),
                gradientId,
                1
        );
    }
    
    public static ModelAttachment resolveAttachment(PlayerSkinPart part, String[] parts, String defaultGradientId) {
        String p1 = parts.length > 1 ? parts[1] : null;
        String p2 = parts.length > 2 ? parts[2] : null;
        
        // Check if p1 is a variant
        if (p1 != null && part.getVariants() != null && part.getVariants().containsKey(p1)) {
            PlayerSkinPart.Variant variant = part.getVariants().get(p1);
            
            // Check if p2 is a texture for the variant
            if (p2 != null && variant.getTextures() != null && variant.getTextures().containsKey(p2)) {
                PlayerSkinPartTexture texture = variant.getTextures().get(p2);
                return new ModelAttachment(
                        variant.getModel(),
                        texture.getTexture(),
                        part.getGradientSet(),
                        defaultGradientId,
                        1
                );
            }
            
            // p2 is not a texture, treat as gradient
            return new ModelAttachment(
                    variant.getModel(),
                    variant.getGreyscaleTexture(),
                    part.getGradientSet(),
                    p2 != null ? p2 : defaultGradientId,
                    1
            );
        }
        
        // Check if p2 is a variant
        if (p2 != null && part.getVariants() != null && part.getVariants().containsKey(p2)) {
            PlayerSkinPart.Variant variant = part.getVariants().get(p2);
            
            // Check if p1 is a texture for the variant
            if (p1 != null && variant.getTextures() != null && variant.getTextures().containsKey(p1)) {
                PlayerSkinPartTexture texture = variant.getTextures().get(p1);
                return new ModelAttachment(
                        variant.getModel(),
                        texture.getTexture(),
                        part.getGradientSet(),
                        defaultGradientId,
                        1
                );
            }
            
            // p1 is not a texture, treat as gradient
            return new ModelAttachment(
                    variant.getModel(),
                    variant.getGreyscaleTexture(),
                    part.getGradientSet(),
                    p1,
                    1
            );
        }
        
        // Check if p1 is a texture (base part)
        if (p1 != null && part.getTextures() != null && part.getTextures().containsKey(p1)) {
            PlayerSkinPartTexture texture = part.getTextures().get(p1);
            return new ModelAttachment(
                    part.getModel(),
                    texture.getTexture(),
                    part.getGradientSet(),
                    p2 != null ? p2 : defaultGradientId,
                    1
            );
        }
        
        // Check if p2 is a texture (base part) (implies p1 is gradient)
        if (p2 != null && part.getTextures() != null && part.getTextures().containsKey(p2)) {
            PlayerSkinPartTexture texture = part.getTextures().get(p2);
            return new ModelAttachment(
                    part.getModel(),
                    texture.getTexture(),
                    part.getGradientSet(),
                    p1,
                    1
            );
        }
        
        return fromPlayerSkinPart(part, p1 != null ? p1 : defaultGradientId);
    }
}
