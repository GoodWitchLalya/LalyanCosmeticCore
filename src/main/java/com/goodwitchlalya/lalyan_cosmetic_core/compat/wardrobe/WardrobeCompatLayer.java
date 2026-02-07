package com.goodwitchlalya.lalyan_cosmetic_core.compat.wardrobe;


import com.goodwitchlalya.lalyan_cosmetic_core.util.AttachmentsRegistry;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.codec.lookup.BuilderCodecMapCodec;
import com.hypixel.hytale.server.core.cosmetics.CosmeticsModule;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

//Since this part of the code is meant to add compatibility with the mod "Wardrobe" https://github.com/jacksonhardaway/wardrobe/tree/main it is obvious some of the code will be similar, especially given the json schema being the same
//Either way, given the license https://github.com/jacksonhardaway/wardrobe/blob/main/LICENSE the mod follows while writing this (07/02/2026 at 16:12), that being GNU Affero General Public License v3.0 and given that our mod follows the same license, it is entirely permissible for me to use this code, given that credits where given (see above line)
public class WardrobeCompatLayer {
    public static final BuilderCodec<WardrobeCompatLayer> CODEC = BuilderCodec.builder(WardrobeCompatLayer.class, WardrobeCompatLayer::new)
        .append(new KeyedCodec<>("CosmeticSlot", Codec.STRING), (a, value) -> a.slot = value, a -> a.slot)
        .add()
        .append(new KeyedCodec<>("Appearance", Appearance.CODEC), (a, value) -> a.appearance = value, (data) -> data.appearance)
        .add()
        .append(new KeyedCodec<>("Properties", Properties.CODEC), (a, value) -> a.properties = value, (data) -> data.properties)
        .add()
        .build();
    
    private Properties properties;
    private String slot;
    private Appearance appearance;

    public Properties getProperties() {
        return properties;
    }

    public String getSlot() {
        return slot;
    }

    public Appearance getAppearance() {
        return appearance;
    }
    
    public static class SlotData {
        public static final BuilderCodec<SlotData> CODEC = BuilderCodec.builder(SlotData.class, SlotData::new)
            .append(new KeyedCodec<>("Properties", Properties.CODEC), (a, value) -> a.properties = value, a -> a.properties)
            .add()
            .append(new KeyedCodec<>("Category", Codec.STRING), (a, value) -> a.category = value, a -> a.category)
            .add()
            .append(new KeyedCodec<>("SelectedIcon", Codec.STRING), (a, value) -> a.selectedIcon = value, a -> a.selectedIcon)
            .add()
            .build();
        
        private Properties properties;
        private String category;
        private String selectedIcon;
        
        public Properties getProperties() {
            return properties;
        }
        
        public String getSelectedIcon() {
            return selectedIcon;
        }
        
        public String getCategory() {
            return category;
        }
    }
    
    public static class Properties {
        public static final BuilderCodec<Properties> CODEC = BuilderCodec.builder(Properties.class, Properties::new)
            .append(new KeyedCodec<>("Icon", Codec.STRING), (a, value) -> a.icon = value, a -> a.icon)
            .add()
            .append(new KeyedCodec<>("Translation", Translation.CODEC), (a, value) -> a.translation = value, a -> a.translation)
            .add()
            .build();
        
        private Translation translation;
        private String icon;
        
        public Translation translation() {
            return translation;
        }
        
        public String icon() {
            return icon;
        }
    }
    
    public static class Translation {
        public static final BuilderCodec<Translation> CODEC = BuilderCodec.builder(Translation.class, Translation::new)
            .append(new KeyedCodec<>("NameKey", Codec.STRING), (a, value) -> a.nameKey = value, a -> a.nameKey)
            .add()
            .append(new KeyedCodec<>("DescriptionKey", Codec.STRING), (a, value) -> a.descriptionKey = value, a -> a.descriptionKey)
            .add()
            .build();
        
        private String nameKey;
        private String descriptionKey;
    }
    
    public interface Appearance {
        BuilderCodecMapCodec<Appearance> CODEC = new BuilderCodecMapCodec<>("Type", true);
        
        String getModel(@Nullable String variantId);
        
        TextureConfig getTextureConfig(@Nullable String optionId);
        
        String[] collectVariants();
    }
    
    public static class ModelAppearance implements Appearance {
        public static final BuilderCodec<ModelAppearance> CODEC = BuilderCodec.builder(ModelAppearance.class, ModelAppearance::new)
            .append(new KeyedCodec<>("Model", Codec.STRING), (a, value) -> a.model = value,a -> a.model)
            .add()
            .append(new KeyedCodec<>("TextureConfig", TextureConfig.CODEC), (a, value) -> a.textureConfig = value, a -> a.textureConfig)
            .add()
            .build();
        
        private String model;
        private TextureConfig textureConfig;
        
        @Override
        public String getModel(String variantId) {
            return model;
        }
        
        @Override
        public TextureConfig getTextureConfig(@Nullable String optionId) {
            return textureConfig;
        }
        
        @Override
        public String[] collectVariants() {
            return new String[0];
        }
    }
    
    public static class VariantAppearance implements Appearance {
        public static final BuilderCodec<VariantAppearance> CODEC = BuilderCodec.builder(VariantAppearance.class, VariantAppearance::new)
            .append(new KeyedCodec<>("Variants", new MapCodec<>(VariantAppearance.Entry.CODEC, LinkedHashMap::new), true), (a, value) -> a.variants = value, a -> a.variants)
            .add()
            .build();
        
        private Map<String, Entry> variants;
        
        public Map<String, VariantAppearance.Entry> getVariants() {
            return variants;
        }
        
        @Override
        public String getModel(String variantId) {
            return variants.get(variantId).getModel();
        }
        
        @Override
        public TextureConfig getTextureConfig(String optionId) {
            Entry entry = variants.get(optionId);
            return entry == null ? null : entry.getTextureConfig();
        }
        
        @Override
        public String[] collectVariants() {
            return this.variants.keySet().toArray(String[]::new);
        }
        
        public static class Entry {
            public static final BuilderCodec<VariantAppearance.Entry> CODEC = BuilderCodec.builder(VariantAppearance.Entry.class, VariantAppearance.Entry::new)
                .append(new KeyedCodec<>("Properties", Properties.CODEC, true), (t, value) -> t.properties = value, t -> t.properties)
                .add()
                .append(new KeyedCodec<>("Icon", Codec.STRING), (t, value) -> t.icon = value, t -> t.icon)
                .add()
                .append(new KeyedCodec<>("Model", Codec.STRING, true), (t, value) -> t.model = value, t -> t.model)
                .add()
                .append(new KeyedCodec<>("TextureConfig", TextureConfig.CODEC, true), (t, value) -> t.textureConfig = value, t -> t.textureConfig)
                .add()
                .build();
            
            private Properties properties;
            private String icon;
            private String model;
            private TextureConfig textureConfig;
            
            public Properties getProperties() {
                return properties;
            }
            
            @Nullable
            public String getIcon() {
                return icon;
            }
            
            public String getModel() {
                return model;
            }
            
            public TextureConfig getTextureConfig() {
                return textureConfig;
            }
        }
    }
    
    public interface TextureConfig {
        BuilderCodecMapCodec<TextureConfig> CODEC = new BuilderCodecMapCodec<>("Type", true);
        
        @Nonnull
        String getTexture(@Nullable String variantId);
        
        String[] collectVariants();
        
        @Nullable
        default String getGradientSet() {
            return null;
        }
    }
    
    public static class StaticTextureConfig implements TextureConfig {
        public static final BuilderCodec<StaticTextureConfig> CODEC = BuilderCodec.builder(StaticTextureConfig.class, StaticTextureConfig::new)
            .append(new KeyedCodec<>("Texture", Codec.STRING, true),
                (t, value) -> t.texture = value,
                t -> t.texture
            )
            .add()
            
            .build();
        
        private String texture;
        
        @Nonnull
        public String getTexture(@Nullable String variantId) {
            return texture;
        }
        
        @Override
        public String[] collectVariants() {
            return new String[0];
        }
    }
    
    public static class GradientTextureConfig implements TextureConfig {
        public static final BuilderCodec<GradientTextureConfig> CODEC = BuilderCodec.builder(GradientTextureConfig.class, GradientTextureConfig::new)
            .append(new KeyedCodec<>("GradientSet", Codec.STRING, true),
                (t, value) -> t.gradientSet = value,
                t -> t.gradientSet
            )
            .add()
            .append(new KeyedCodec<>("GrayscaleTexture", Codec.STRING, true),
                (t, value) -> t.grayscaleTexture = value,
                t -> t.grayscaleTexture
            )
            .add()
            .build();
        
        private String gradientSet;
        private String grayscaleTexture;
        
        private GradientTextureConfig() {
        }
        
        public GradientTextureConfig(String gradientSet, String grayscaleTexture) {
            this.gradientSet = gradientSet;
            this.grayscaleTexture = grayscaleTexture;
        }
        
        @Nonnull
        @Override
        public String getTexture(@Nullable String variantId) {
            return grayscaleTexture;
        }
        
        @Nonnull
        @Override
        public String getGradientSet() {
            return gradientSet;
        }
        
        @Override
        public String[] collectVariants() {
            return CosmeticsModule.get().getRegistry().getGradientSets().get(this.getGradientSet()).getGradients().keySet().toArray(String[]::new);
        }
    }
    
    public static class VariantTextureConfig implements TextureConfig {
        public static final BuilderCodec<VariantTextureConfig> CODEC = BuilderCodec.builder(VariantTextureConfig.class, VariantTextureConfig::new)
            .append(new KeyedCodec<>("Variants", new MapCodec<>(Entry.CODEC, LinkedHashMap::new), true), (t, value) -> t.variants = value, t -> t.variants).add()
            .build();
        
        private Map<String, Entry> variants;
        
        public Map<String, Entry> getVariants() {
            return variants;
        }
        
        @Nonnull
        @Override
        public String getTexture(@Nullable String variantId) {
            Entry entry = this.getVariants().get(variantId);
            return entry.getTexture();
        }
        
        @Override
        public String[] collectVariants() {
            return this.variants.keySet().toArray(String[]::new);
        }
        
        public Map<String, AttachmentsRegistry.Variant> getFormattedVariants() {
            Map<String, AttachmentsRegistry.Variant> toRet = new HashMap<>();
            
            this.variants.forEach((key, value) -> {
                toRet.put(key, new AttachmentsRegistry.Variant(value.getTexture(), value.getIcon()));
            });
            
            return toRet;
        }
        
        public static class Entry {
            public static final BuilderCodec<Entry> CODEC = BuilderCodec.builder(Entry.class, Entry::new)
                .append(new KeyedCodec<>("Properties", Properties.CODEC, true), (t, value) -> t.properties = value, t -> t.properties)
                .add()
                .append(new KeyedCodec<>("Icon", Codec.STRING), (t, value) -> t.icon = value, t -> t.icon)
                .add()
                .append(new KeyedCodec<>("Texture", Codec.STRING, true), (t, value) -> t.texture = value, t -> t.texture)
                .add()
                .append(new KeyedCodec<>("WardrobeColor", Codec.STRING_ARRAY, true), (t, value) -> t.colors = value, t -> t.colors)
                .add()
                .build();
            
            private Properties properties;
            private String icon;
            private String texture;
            private String[] colors;
            
            public Properties getProperties() {
                return properties;
            }
            
            @Nullable
            public String getIcon() {
                return icon;
            }
            
            @Nonnull
            public String getTexture() {
                return texture;
            }
            
            
            @Nonnull
            public String[] getColors() {
                return colors;
            }
        }
    }
}
