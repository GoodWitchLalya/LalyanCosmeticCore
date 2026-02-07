package com.goodwitchlalya.lalyan_cosmetic_core.util;

import com.goodwitchlalya.lalyan_cosmetic_core.CosmeticCore;
import com.goodwitchlalya.lalyan_cosmetic_core.component.CosmeticData;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.Direction;
import com.hypixel.hytale.protocol.PlayerSkin;
import com.hypixel.hytale.protocol.Position;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAttachment;
import com.hypixel.hytale.server.core.cosmetics.CosmeticRegistry;
import com.hypixel.hytale.server.core.cosmetics.CosmeticsModule;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSkinComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

// A singleton registry that manages all custom cosmetic and character attachments.
// It handles loading, storing, and applying these attachments to player models.
public class AttachmentsRegistry {
    
    // Singleton instance of the registry.
    private static AttachmentsRegistry INSTANCE;
    
    // The main map storing all registered attachments, keyed by a unique ID (e.g., "assetpack#cosmeticName").
    private final Map<String, Attachment> attachmentsRegistry = new HashMap<>();
    
    // A list of slots that doesn't make the override by default
    public final List<Slot> nonOverridingSlots = new ArrayList<>();
    
    public List<SlotConnection> connections = new ArrayList<>();
    
    public void registerTLC(TopLevelCategory tlc) {
        if (tlcFromName(tlc.name) != null) return;
        
        topLevelCategories.add(tlc);
    }
    
    public TopLevelCategory tlcFromName(String tlc) {
        return topLevelCategories.stream()
            .filter(t -> t.name.equals(tlc))
            .findFirst().orElse(null);
    }
    
    public static class TopLevelCategory {
        public static final BuilderCodec<TopLevelCategory> CODEC = BuilderCodec.builder(TopLevelCategory.class, TopLevelCategory::new)
            .append(new KeyedCodec<>("Name", Codec.STRING), (data, value) -> data.name = value, (data) -> data.name)
            .add()
            .build();
        
        public String name;
    }
    
    private final List<TopLevelCategory> topLevelCategories = new ArrayList<>();
    
    public List<TopLevelCategory> getTopLevelCategories() {
        return topLevelCategories;
    }
    
    public static class Slot {
        public static final BuilderCodec<Slot> CODEC = BuilderCodec.builder(Slot.class, Slot::new)
            .append(new KeyedCodec<>("Name", Codec.STRING), (data, value) -> data.name = value, data -> data.name)
            .add()
            .append(new KeyedCodec<>("Icon", Codec.STRING), (data, value) -> data.icon = value, data -> data.icon)
            .add()
            .append(new KeyedCodec<>("SelectedIcon", Codec.STRING), (data, value) -> data.selectedIcon = value, data -> data.selectedIcon)
            .add()
            .append(new KeyedCodec<>("TopLevelCategory", Codec.STRING), (data, value) -> data.tlcName = value, data -> data.tlcName)
            .add()
            .append(new KeyedCodec<>("Camera", SlotCameraProperties.CODEC), (data, value) -> data.camera = value, data -> data.camera)
            .add()
            .append(new KeyedCodec<>("CanVanish", BuilderCodec.BOOLEAN), (data, value) -> data.canVanish = value, data -> data.canVanish)
            .add()
            .append(new KeyedCodec<>("AlternativeName", Codec.STRING_ARRAY), (data, value) -> data.alternativeName = value != null ? Arrays.asList(value) : null, (data) -> data.alternativeName != null ? data.alternativeName.toArray(new String[0]) : null)
            .add()
            .build();
        
        public String name;
        public String icon, selectedIcon;
        public String tlcName;
        
        public SlotCameraProperties camera;
        
        public boolean canVanish;
        
        public List<String> alternativeName;
    }
    
    private final List<Slot> slots = new ArrayList<>();
    
    public void registerSlot(Slot slot) {
        if (slotFromName(slot.name) != null) return;
        
        slots.add(slot);
        
        if (slot.name.equals("Hair_Extension")) {
            nonOverridingSlots.add(slot);
        }
        
        if (slot.name.equals("Haircuts")) {
            connections.add(new SlotConnection(slotFromName("Hair_Extension"), slot, "Hair", (skin) -> skin.haircut.split("\\.")[1]));
        }
        
        if (slot.name.equals("Mouths")) {
            connections.add(new SlotConnection(slot, null, "Skin", (skin) -> skin.bodyCharacteristic.split("\\.")[1]));
        }
    }
    
    public List<Slot> getSlots() {
        return slots;
    }
    
    public List<Slot> slotFromTopLevelCategory(TopLevelCategory tlc) {
        return slots.stream()
            .filter(s -> s.tlcName.equals(tlc.name))
            .sorted(Comparator.comparing(t -> t.name))
            .toList();
    }
    
    public Slot slotFromName(String name) {
        return slots.stream()
            .filter(s -> s.name.equals(name) || (s.alternativeName != null && s.alternativeName.contains(name))).min(Comparator.comparing(s -> s.name)).orElse(null);
    }
    
    public static class SlotCameraProperties {
        public static final BuilderCodec<Position> POSITION_CODEC = BuilderCodec.builder(Position.class, Position::new)
            .append(new KeyedCodec<>("X", BuilderCodec.DOUBLE), (data, value) -> data.x = value, (data) -> data.x)
            .add()
            .append(new KeyedCodec<>("Y", BuilderCodec.DOUBLE), (data, value) -> data.y = value, (data) -> data.y)
            .add()
            .append(new KeyedCodec<>("Z", BuilderCodec.DOUBLE), (data, value) -> data.z = value, data -> data.z)
            .add()
            .build();
        
        public static final BuilderCodec<Direction> DIRECTION_CODEC = BuilderCodec.builder(Direction.class, Direction::new)
            .append(new KeyedCodec<>("Yaw", BuilderCodec.FLOAT), (data, value) -> data.yaw = value, (data) -> data.yaw)
            .add()
            .append(new KeyedCodec<>("Pitch", BuilderCodec.FLOAT), (data, value) -> data.pitch = value, (data) -> data.pitch)
            .add()
            .append(new KeyedCodec<>("Roll", BuilderCodec.FLOAT), (data, value) -> data.roll = value, data -> data.roll)
            .add()
            .build();
        
        public static final BuilderCodec<SlotCameraProperties> CODEC = BuilderCodec.builder(SlotCameraProperties.class, SlotCameraProperties::new)
            .append(new KeyedCodec<>("Distance", BuilderCodec.INTEGER), (data, value) -> data.distance = value, (data) -> data.distance)
            .add()
            .append(new KeyedCodec<>("Position_Offset", POSITION_CODEC), (data, value) -> data.positionOffset = value, data -> data.positionOffset)
            .add()
            .append(new KeyedCodec<>("Rotation", DIRECTION_CODEC), (data, value) -> data.rotation = value, data -> data.rotation)
            .add()
            .append(new KeyedCodec<>("LookAtBack", BuilderCodec.BOOLEAN), (data, value) -> data.lookAtBack = value, data -> data.lookAtBack)
            .add()
            .build();
        
        public int distance;
        public Position positionOffset;
        public Direction rotation;
        public boolean lookAtBack;
    }
    
    public record GradientSet(String name, List<String> colourList) {
        public GradientSet(String name) {
            this(name, new ArrayList<>());
        }
        
        public boolean add(String colour) {
            return colourList.add(colour);
        }
        
        public boolean remove(String colour) {
            return colourList.remove(colour);
        }
        
        public boolean containsColour(String colour) {
            return colourList.contains(colour);
        }
        
        @Override
        public String toString() {
            return String.format("%s: [%s]", name, colourList);
        }
    }
    
    public static class ColoursDataSet {
        private final List<GradientSet> gradientSets;
        
        public ColoursDataSet(List<GradientSet> gradientSets) {
            this.gradientSets = gradientSets;
        }
        
        public ColoursDataSet() {
            this.gradientSets = new ArrayList<>();
        }
        
        public boolean add(GradientSet gradientSet) {
            return gradientSets.add(gradientSet);
        }
        
        public boolean remove(GradientSet gradientSet) {
            return gradientSets.remove(gradientSet);
        }
        
        public boolean contains(GradientSet gradientSet) {
            return gradientSets.contains(gradientSet);
        }
        
        public boolean contains(String gradientSet) {
            return gradientSets.stream().anyMatch((set) -> set.name.equals(gradientSet));
        }
        
        public GradientSet getGradientSet(String gradientSetName) {
            return gradientSets.stream().filter((set) -> set.name.equals(gradientSetName)).findFirst().orElse(null);
        }
        
    }
    
    public static ColoursDataSet coloursDataSet = new ColoursDataSet();
    
    private static final GradientSet coloredCottonSet = new GradientSet("Colored_Cotton");
    
    public enum coloredCottonSetNames {
        Black,
        Blue,
        Brown,
        Charcoal,
        Cream,
        Green,
        Grey,
        Lime,
        Orange,
        Pink,
        Purple,
        Red,
        Turquoise,
        White,
        Yellow
    }
    
    private static final GradientSet eyesGradientSet = new GradientSet("Eyes_Gradient");
    
    public enum eyesGradientSetNames {
        Black,
        Blond,
        Blue,
        BlueLight,
        Brown,
        BrownDark,
        BrownLight,
        Green,
        GreenLight,
        Grey,
        Honey,
        Orange,
        Pink,
        Purple,
        Red,
        RedDark,
        Turquoise,
        White
    }
    
    private static final GradientSet fadedLeatherSet = new GradientSet("Faded_Leather");
    
    public enum fadedLeatherSetNames {
        Black,
        Blue,
        BlueDark,
        Brown,
        BrownDark,
        Green,
        Grey,
        Lime,
        Orange,
        Orange_Tan,
        Pink,
        Purple,
        Red,
        Turquoise,
        Violet,
        White,
        Yellow
    }
    
    private static final GradientSet fantasyCottonSet = new GradientSet("Fantasy_Cotton");
    
    public enum fantasyCottonSetNames {
        Beige,
        Black,
        Blue,
        Brown,
        Green,
        Lime,
        Orange,
        Pink,
        Purple,
        Red,
        Turquoise,
        Yellow
    }
    
    private static final GradientSet fantasyCottonDarkSet = new GradientSet("Fantasy_Cotton_Dark");
    
    public enum fantasyCottonDarkSetNames {
        Black,
        Blue,
        BlueDark,
        Brown,
        Green,
        Lime,
        Orange,
        Pink,
        Purple,
        Red,
        Turquoise,
        Yellow
    }
    
    private static final GradientSet flashySyntheticSet = new GradientSet("Flashy_Synthetic");
    
    public enum flashySyntheticSetNames {
        Black,
        Blue,
        Green,
        Grey,
        Orange,
        OrangePastel,
        Pink,
        PinkPastel,
        Purple,
        Red,
        Turquoise,
        Violet,
        White,
        Yellow
    }
    
    private static final GradientSet hairSet = new GradientSet("Hair");
    
    public enum hairSetNames {
        Black,
        Blond,
        BlondCaramel,
        BlondPlatinum,
        BlondSand,
        Blue,
        Blue_Anthracite,
        BlueDark,
        BlueLight,
        Brown,
        BrownDark,
        BrownDarker,
        BrownLight,
        BrownSemiDark,
        BrownSemiLight,
        Bubblegum,
        Copper,
        Green,
        Grey,
        GreyAsh,
        GreyPurple,
        Lavender,
        Pink,
        PinkBerry,
        PitchBlack,
        Purple,
        Red,
        RedDark,
        Turquoise,
        White
    }
    
    private static final GradientSet jeanGenericSet = new GradientSet("Jean_Generic");
    
    public enum jeanGenericSetNames {
        Black,
        Blue,
        Blue_Night,
        BluePastel,
        GreyBlue,
        GreyDark,
        GreyLight,
        Marine_Blue,
        Maroon,
        Turquoise_Dark
    }
    
    private static final GradientSet ornamentedMetalSet = new GradientSet("Ornamented_Metal");
    
    public enum ornamentedMetalSetNames {
        Brass_Purple,
        Copper_Green,
        Gold_Red,
        Iron_Black,
        Silver_Blue
    }
    
    private static final GradientSet pastelCottonSet = new GradientSet("Pastel_Cotton");
    
    public enum pastelCottonSetNames {
        Black,
        Blue,
        Carmin,
        Green,
        Grey,
        Lime,
        Orange,
        Pink,
        PinkPastel,
        Purple,
        PurplePastel,
        Red,
        Turquoise,
        White,
        Yellow
    }
    
    private static final GradientSet rottenFabricSet = new GradientSet("Rotten_Fabric");
    
    public enum rottenFabricSetNames {
        Blue,
        Brown,
        Yellow
    }
    
    private static final GradientSet shinyFabricSet = new GradientSet("Shiny_Fabric");
    
    public enum shinyFabricSetNames {
        Black,
        Blue,
        Brown,
        Green,
        Grey,
        Lime,
        Orange,
        Pink,
        Purple,
        Red,
        Turquoise,
        Violet,
        White,
        Yellow
    }
    
    private static final GradientSet skinSet = new GradientSet("Skin");
    
    public enum skinSetNames {
        s01, s02, s03, s04, s05, s06, s07, s08, s09, s10,
        s11, s12, s13, s14, s15, s16, s17, s18, s19, s20,
        s21, s22, s23, s24, s25, s26, s27, s28, s29, s30,
        s31, s32, s33, s34, s35, s36, s37, s38, s39, s40,
        s41, s42, s43, s44, s45, s46, s47, /*s48, s49, s50,
        s51, s52*/;
        
        public String getName() {
            return this.name().replace("s", "");
        }
    }
    
    static {
        /* Adding Colored_Cotton colours to the set */
        Arrays.stream(coloredCottonSetNames.values()).forEach(value -> coloredCottonSet.add(value.name()));
        
        /* Adding Eyes_Gradient colours to the set */
        Arrays.stream(eyesGradientSetNames.values()).forEach(value -> eyesGradientSet.add(value.name()));
        
        /* Adding Faded_Leather colours to the set */
        Arrays.stream(fadedLeatherSetNames.values()).forEach(value -> fadedLeatherSet.add(value.name()));
        
        /* Adding Fantasy_Cotton colours to the set */
        Arrays.stream(fantasyCottonSetNames.values()).forEach(value -> fantasyCottonSet.add(value.name()));
        
        /* Adding Fantasy_Cotton_Dark colours to the set */
        Arrays.stream(fantasyCottonDarkSetNames.values()).forEach(value -> fantasyCottonDarkSet.add(value.name()));
        
        /* Adding Flashy_Synthetic colours to the set */
        Arrays.stream(flashySyntheticSetNames.values()).forEach(value -> flashySyntheticSet.add(value.name()));
        
        /* Adding Hair colours to the set */
        Arrays.stream(hairSetNames.values()).forEach(value -> hairSet.add(value.name()));
        
        /* Adding Jean_Generic colours to the set */
        Arrays.stream(jeanGenericSetNames.values()).forEach(value -> jeanGenericSet.add(value.name()));
        
        /* Adding Ornamented_Metal colours to the set */
        Arrays.stream(ornamentedMetalSetNames.values()).forEach(value -> ornamentedMetalSet.add(value.name()));
        
        /* Adding Pastel_Cotton colours to the set */
        Arrays.stream(pastelCottonSetNames.values()).forEach(value -> pastelCottonSet.add(value.name()));
        
        /* Adding Rotten_Fabric colours to the set */
        Arrays.stream(rottenFabricSetNames.values()).forEach(value -> rottenFabricSet.add(value.name()));
        
        /* Adding Shiny_Fabric colours to the set */
        Arrays.stream(shinyFabricSetNames.values()).forEach(value -> shinyFabricSet.add(value.name()));
        
        /* Adding Skin colours to the set */
        Arrays.stream(skinSetNames.values()).forEach(value -> skinSet.add(value.getName()));
        
        coloursDataSet.add(coloredCottonSet);
        coloursDataSet.add(eyesGradientSet);
        coloursDataSet.add(fadedLeatherSet);
        coloursDataSet.add(fantasyCottonSet);
        coloursDataSet.add(fantasyCottonDarkSet);
        coloursDataSet.add(flashySyntheticSet);
        coloursDataSet.add(hairSet);
        coloursDataSet.add(jeanGenericSet);
        coloursDataSet.add(ornamentedMetalSet);
        coloursDataSet.add(pastelCottonSet);
        coloursDataSet.add(rottenFabricSet);
        coloursDataSet.add(shinyFabricSet);
        coloursDataSet.add(skinSet);
    }
    
    // Provides access to the singleton instance of the registry.
    public static AttachmentsRegistry get() {
        if (INSTANCE == null) INSTANCE = new AttachmentsRegistry();
        
        return INSTANCE;
    }
    
    // Returns the raw map of registered attachments.
    public Map<String, Attachment> getAttachmentsRegistry() {
        return attachmentsRegistry;
    }
    
    // A record to hold data for a single cosmetic variant (texture and icon).
    public static class Variant {
        public static final BuilderCodec<Variant> CODEC = BuilderCodec.builder(Variant.class, Variant::new)
            .append(new KeyedCodec<>("Texture", Codec.STRING), (data, value) -> data.texture = value, (data) -> data.texture)
            .add()
            .append(new KeyedCodec<>("Icon", Codec.STRING), (data, value) -> data.icon = value, (data) -> data.icon)
            .add()
            .build();
        
        public String texture;
        public String icon;
        
        public Variant() {
        }
        
        public Variant(String texture, String icon) {
            this.texture = texture;
            this.icon = icon;
        }
        
        public String texture() {
            return texture;
        }
        
        public String icon() {
            return icon;
        }
    }
    
    public record SlotConnection(Slot mainSlot, Slot connectedSlot, String gradientSet,
                                 Function<PlayerSkin, String> defaultId) {
    }
    
    public static class Alternative {
        public static final BuilderCodec<Alternative> CODEC = BuilderCodec.builder(Alternative.class, Alternative::new)
            .append(new KeyedCodec<>("Gradient_Set", Codec.STRING), (data, value) -> data.gradientSet = value, (data) -> data.gradientSet)
            .add()
            .append(new KeyedCodec<>("Variants", new MapCodec<>(Variant.CODEC, Object2ObjectOpenHashMap::new)), (data, value) -> data.variants = value, (data) -> data.variants)
            .add()
            .build();
        
        public String gradientSet;
        public Map<String, Variant> variants;
    }
    
    public static class Colour {
        public static final BuilderCodec<Colour> CODEC = BuilderCodec.builder(Colour.class, Colour::new)
            .append(new KeyedCodec<>("Gradient_Set", Codec.STRING), (data, value) -> data.gradientSet = value, (data) -> data.gradientSet)
            .add()
            .append(new KeyedCodec<>("Gradient_Id", Codec.STRING), (data, value) -> data.gradientID = value, (data) -> data.gradientID)
            .add()
            .build();
        
        public String gradientSet;
        public String gradientID;
        
        public Colour(String gradientSet, String gradientID) {
            set(gradientSet, gradientID);
        }
        
        public Colour(String gradient) {
            this.set(gradient);
        }
        
        public Colour() {
        }
        
        public void set(String gradient) {
            String[] split = gradient.split(":");
            set(split[0], split[1]);
        }
        
        public void set(String gradientSet, String gradientID) {
            this.gradientSet = gradientSet;
            this.gradientID = gradientID;
        }
        
        public void setGradientSet(String gradientSet) {
            set(gradientSet, this.gradientID);
        }
        
        public void setGradientID(String gradientID) {
            set(this.gradientSet, gradientID);
        }
        
        public String getGradientSet() {
            return gradientSet;
        }
        
        public String getGradientID() {
            return gradientID;
        }
        
        public String getF() {
            return gradientSet + ":" + gradientID;
        }
        
        public static Colour empty() {
            return new Colour("", "");
        }
        
        @Override
        public String toString() {
            return getF();
        }
        
        public String apply(String cosmeticId) {
            if (gradientSet.isBlank() || gradientID.isBlank()) return cosmeticId;
            
            return cosmeticId + "%" + gradientSet + ":" + gradientID;
        }
    }
    
    // A class holding all the data for a single attachment, loaded from asset files or JSON.
    // This includes paths to model, texture, icon, as well as variants and slot overrides.
    public static class AttachmentData {
        public static final BuilderCodec<AttachmentData> CODEC = BuilderCodec.builder(AttachmentData.class, AttachmentData::new)
            .append(new KeyedCodec<>("Model", Codec.STRING), (data, value) -> data.model = value, (data) -> data.model)
            .add()
            .append(new KeyedCodec<>("Texture", Codec.STRING), (data, value) -> data.texture = value, (data) -> data.texture)
            .add()
            .append(new KeyedCodec<>("Icon", Codec.STRING), (data, value) -> data.icon = value, (data) -> data.icon)
            .add()
            .append(new KeyedCodec<>("Alternatives", Alternative.CODEC), (data, value) -> data.alternatives = value, (data) -> data.alternatives)
            .add()
            .append(new KeyedCodec<>("Slot_Overrides", Codec.STRING_ARRAY), (data, value) -> data.slotOverrides = value != null ? Arrays.asList(value) : null, (data) -> data.slotOverrides != null ? data.slotOverrides.toArray(new String[0]) : null)
            .add()
            .append(new KeyedCodec<>("Default_Color", Colour.CODEC), (data, value) -> data.defaultColor = value, (data) -> data.defaultColor)
            .add()
            // Legacy support
            .append(new KeyedCodec<>("Variants", new MapCodec<>(Variant.CODEC, Object2ObjectOpenHashMap::new)), (data, value) -> {
                if (data.alternatives == null) data.alternatives = new Alternative();
                if (data.alternatives.variants == null) data.alternatives.variants = value;
            }, (data) -> null)
            .add()
            .append(new KeyedCodec<>("Gradient_Set", Codec.STRING), (data, value) -> {
                if (data.alternatives == null) data.alternatives = new Alternative();
                if (data.alternatives.gradientSet == null) data.alternatives.gradientSet = value;
            }, (data) -> null)
            .add()
            .build();
        
        public String model;
        public String texture;
        public String icon;
        public Alternative alternatives;
        public List<String> slotOverrides;
        public Colour defaultColor;
        
        public Slot slot; // The primary slot this attachment belongs to.
        
        public AttachmentData() {
        }
        
        public AttachmentData(String model, String texture, String icon, Map<String, Variant> variants, String gradientSet, List<String> slotOverrides, Colour defaultColor) {
            this.model = model;
            this.texture = texture;
            this.icon = icon;
            this.slotOverrides = slotOverrides;
            this.defaultColor = defaultColor;
            
            this.alternatives = new Alternative();
            
            if (variants != null && !variants.isEmpty()) {
                this.alternatives.variants = variants;
                return;
            }
            
            if (gradientSet != null && !gradientSet.isEmpty()) {
                this.alternatives.gradientSet = gradientSet;
            }
        }
        
        public String model() {
            return model;
        }
        
        public String texture() {
            return texture;
        }
        
        public String icon() {
            return icon;
        }
        
        public Slot slot() {
            return slot;
        }
        
        public Map<String, Variant> variants() {
            return alternatives != null && alternatives.variants != null ? alternatives.variants : Map.of();
        }
        
        public List<String> slotOverrides() {
            return slotOverrides != null ? slotOverrides : List.of();
        }
        
        public String gradientSet() {
            return alternatives != null && alternatives.gradientSet != null ? alternatives.gradientSet : "";
        }
        
        public Colour defaultColor() {
            return defaultColor;
        }
    }
    
    // A record representing a fully processed attachment, containing its name and its data.
    public record Attachment(String name, AttachmentData data) {
        // Creates a Hytale ModelAttachment object from this attachment's data.
        // @param variant The name of the variant to use. If empty, the default texture is used.
        // @return A ModelAttachment ready to be applied to a player model.
        private ModelAttachment makeModel(String variant, Colour colour) {
            if (variant.isEmpty()) {
                return new ModelAttachment(
                    data.model(),
                    data.texture(),
                    colour.getGradientSet(),
                    colour.getGradientID(),
                    1
                );
            }
            
            Variant v = data.variants().get(variant);
            
            return new ModelAttachment(
                data.model(),
                v.texture(),
                colour.getGradientSet(),
                colour.getGradientID(),
                1
            );
        }
        
        public ModelAttachment makeModel(String variant) {
            return makeModel(variant, new Colour("", ""));
        }
        
        public ModelAttachment makeModel(String set, String id) {
            return makeModel("", new Colour(set, id));
        }
        
        public List<ModelAttachment> makeModelsWithColorSet(String colourSet) {
            List<ModelAttachment> internal = new ArrayList<ModelAttachment>();
            GradientSet gradientSet = coloursDataSet.getGradientSet(colourSet);
            
            gradientSet.colourList.forEach(colour -> {
                internal.add(makeModel("", new Colour(colourSet, colour)));
            });
            
            return internal;
        }
    }
    
    // The core method for rebuilding a player's skin. It combines the player's default skin
    // with the custom cosmetics they have equipped.
    // @param ref A reference to the player entity.
    public void rebuildSkinWithCosmetics(Ref<EntityStore> ref) {
        Store<EntityStore> store = ref.getStore();
        
        CosmeticData data = store.getComponent(ref, CosmeticData.INSTANCE);
        Player player = store.getComponent(ref, Player.getComponentType());
        Model model = store.getComponent(ref, ModelComponent.getComponentType()).getModel();
        PlayerSkin playerSkin = store.getComponent(ref, PlayerSkinComponent.getComponentType()).getPlayerSkin();
        
        if (data == null) {
            store.addComponent(ref, CosmeticData.INSTANCE, new CosmeticData());
            return;
        }
        
        List<ModelAttachment> attachments = new ArrayList<>();
        Map<Slot, Boolean> overrides = new HashMap<>();
        List<String> invalid = new ArrayList<>();
        
        // Iterate through the player's equipped cosmetics.
        for (String cosmetic : data.getCosmetics()) {
            // Handle "empty slot" markers.
            Slot slot = slotFromName(cosmetic.replace("No", ""));
            
            if (slot != null) {
                overrides.put(slot, true);
                continue;
            }
            
            // Parse cosmetic ID and variant name.
            String cosmId = cosmetic;
            String variant = "";
            
            String gradientSet = "";
            String gradientId = "";
            
            if (cosmetic.contains("$")) {
                String[] split = cosmetic.split("\\$");
                cosmId = split[0];
                variant = split[1];
            } else if (cosmetic.contains("%")) {
                String[] gradStuff = cosmetic.split("%");
                String[] split = gradStuff[1].split(":");
                
                CosmeticCore.log(String.format("cosmetic: %s", cosmetic));// DEBUG
                cosmId = gradStuff[0];
                gradientSet = split[0];
                gradientId = split[1];
            }
            
            // Find the attachment in the registry.
            Attachment attachment = attachmentsRegistry.get(cosmId);
            if (attachment == null) {
                invalid.add(cosmId);
                continue;
            }
            
            // Add the attachment's primary slot and any extra override slots to the override map.
            if (nonOverridingSlots.contains(attachment.data().slot())) {
                overrides.put(attachment.data().slot(), false);
            }
            
            SlotConnection connection = connections.stream()
                .filter(c -> c.mainSlot() == attachment.data().slot())
                .findFirst().orElse(null);
            
            if (connection != null) {
                if (connection.gradientSet == null && connection.connectedSlot == null)
                    throw new IllegalArgumentException("Cannot have a connection with no gradient set nor connected slot!");
                
                gradientSet = connection.gradientSet();
                CosmeticData.FormattedItemData itemData = null;
                
                if (connection.connectedSlot() != null) itemData = data.getCosmetic(connection.connectedSlot());
                
                if (itemData != null) {
                    gradientSet = itemData.getColour().getGradientSet();
                    gradientId = itemData.getColour().getGradientID();
                } else {
                    gradientId = connection.defaultId.apply(playerSkin);
                }
            } else if (gradientSet.isEmpty() && !attachment.data().gradientSet().isEmpty()) {
                String set = attachment.data().gradientSet();
                GradientSet gs = coloursDataSet.getGradientSet(set);
                if (gs != null && !gs.colourList().isEmpty()) {
                    gradientSet = set;
                    gradientId = gs.colourList().get(0);
                }
            }
            
            // Create the model attachment and add it to the list.
            attachments.add(attachment.makeModel(variant, new Colour(gradientSet, gradientId)));
        }
        
        // Clean up any invalid cosmetics from the player's data.
        for (String inv : invalid) {
            data.removeCosmetic(inv);
        }
        
        // Restore the base skin, skipping parts that are overridden by custom cosmetics.
        restoreSkinWithOverrides(ref, attachments, overrides);
        store.replaceComponent(ref, CosmeticData.INSTANCE, data);
        
        // Create a new player model with the combined attachments.
        Model newModel = new Model(player.getDisplayName() + "_CustomModel", model.getScale(), model.getRandomAttachmentIds(), attachments.toArray(new ModelAttachment[0]), model.getBoundingBox(), model.getModel(), model.getTexture(), model.getGradientSet(), model.getGradientId(), model.getEyeHeight(), model.getCrouchOffset(), model.getAnimationSetMap(), model.getCamera(), model.getLight(), model.getParticles(), model.getTrails(), model.getPhysicsValues(), model.getDetailBoxes(), model.getPhobia(), model.getPhobiaModelAssetId());
        
        // Apply the new model to the player.
        store.replaceComponent(ref, ModelComponent.getComponentType(), new ModelComponent(newModel));
        store.replaceComponent(ref, CosmeticData.INSTANCE, data);
    }
    
    // Re-applies the player's default Hytale skin parts (hair, eyes, etc.) unless they
    // are marked as being overridden by a custom cosmetic.
    private void restoreSkinWithOverrides(Ref<EntityStore> ref, List<ModelAttachment> attachments, Map<Slot, Boolean> overrides) {
        CosmeticRegistry registry = CosmeticsModule.get().getRegistry();
        
        Store<EntityStore> store = ref.getStore();
        PlayerSkin playerSkin = store.getComponent(ref, PlayerSkinComponent.getComponentType()).getPlayerSkin();
        
        String gradientId = playerSkin.bodyCharacteristic.split("\\.")[1];
        String[] bodyCharacteristicParts = playerSkin.bodyCharacteristic.split("\\.");
        
        // This large block of code checks each vanilla cosmetic slot. If it's not in the 'overrides' map,
        // it resolves the corresponding attachment from the vanilla registry and adds it to the list.
        var bodyCharacteristic = registry.getBodyCharacteristics().get(bodyCharacteristicParts[0]);
        if (bodyCharacteristic != null) {
            attachments.add(ModelUtils.resolveAttachment(bodyCharacteristic, bodyCharacteristicParts, gradientId));
        }
        
        if (!overrides.getOrDefault(slotFromName("Beards"), false)) {
            if (playerSkin.facialHair != null) {
                String[] facialHairsParts = playerSkin.facialHair.split("\\.");
                var facialHairs = registry.getFacialHairs().get(facialHairsParts[0]);
                if (facialHairs != null) {
                    attachments.add(ModelUtils.resolveAttachment(facialHairs, facialHairsParts, gradientId));
                }
            }
        }
        
        if (!overrides.getOrDefault(slotFromName("Ears"), false)) {
            if (playerSkin.ears != null) {
                String[] earsParts = playerSkin.ears.split("\\.");
                var ears = registry.getEars().get(earsParts[0]);
                if (ears != null) {
                    attachments.add(ModelUtils.resolveAttachment(ears, earsParts, playerSkin.bodyCharacteristic.split("\\.")[1]));
                }
            }
        }
        
        if (!overrides.getOrDefault(slotFromName("Eyebrows"), false)) {
            if (playerSkin.eyebrows != null) {
                String[] eyebrowsParts = playerSkin.eyebrows.split("\\.");
                var eyebrows = registry.getEyebrows().get(eyebrowsParts[0]);
                if (eyebrows != null) {
                    attachments.add(ModelUtils.resolveAttachment(eyebrows, eyebrowsParts, gradientId));
                }
            }
        }
        
        if (!overrides.getOrDefault(slotFromName("Eyes"), false)) {
            if (playerSkin.eyes != null) {
                String[] eyesParts = playerSkin.eyes.split("\\.");
                var eyes = registry.getEyes().get(eyesParts[0]);
                if (eyes != null) {
                    attachments.add(ModelUtils.resolveAttachment(eyes, eyesParts, gradientId));
                }
            }
        }
        
        if (!overrides.getOrDefault(slotFromName("Faces"), false)) {
            if (playerSkin.face != null) {
                String[] faceParts = playerSkin.face.split("\\.");
                var face = registry.getFaces().get(faceParts[0]);
                if (face != null) {
                    attachments.add(ModelUtils.resolveAttachment(face, faceParts, playerSkin.bodyCharacteristic.split("\\.")[1]));
                }
            }
        }
        
        if (!overrides.getOrDefault(slotFromName("Mouths"), false)) {
            if (playerSkin.mouth != null) {
                String[] mouthsParts = playerSkin.mouth.split("\\.");
                var mouths = registry.getMouths().get(mouthsParts[0]);
                if (mouths != null) {
                    attachments.add(ModelUtils.resolveAttachment(mouths, mouthsParts, gradientId));
                }
            }
        }
        
        if (!overrides.getOrDefault(slotFromName("Haircuts"), false)) {
            if (playerSkin.haircut != null) {
                String[] haircutsParts = playerSkin.haircut.split("\\.");
                var haircuts = registry.getHaircuts().get(haircutsParts[0]);
                if (haircuts != null) {
                    attachments.add(ModelUtils.resolveAttachment(haircuts, haircutsParts, gradientId));
                }
            }
        }
        
        /* Cosmetics Slots */
        if (!overrides.getOrDefault(slotFromName("Capes"), false)) {
            if (playerSkin.cape != null) {
                String[] capesParts = playerSkin.cape.split("\\.");
                var capes = registry.getCapes().get(capesParts[0]);
                if (capes != null) {
                    attachments.add(ModelUtils.resolveAttachment(capes, capesParts, gradientId));
                }
            }
        }
        
        if (!overrides.getOrDefault(slotFromName("Face_Accessories"), false)) {
            if (playerSkin.faceAccessory != null) {
                String[] faceAccessoriesParts = playerSkin.faceAccessory.split("\\.");
                var faceAccessories = registry.getFaceAccessories().get(faceAccessoriesParts[0]);
                if (faceAccessories != null) {
                    attachments.add(ModelUtils.resolveAttachment(faceAccessories, faceAccessoriesParts, gradientId));
                }
            }
        }
        
        if (!overrides.getOrDefault(slotFromName("Gloves"), false)) {
            if (playerSkin.gloves != null) {
                String[] glovesParts = playerSkin.gloves.split("\\.");
                var gloves = registry.getGloves().get(glovesParts[0]);
                if (gloves != null) {
                    attachments.add(ModelUtils.resolveAttachment(gloves, glovesParts, gradientId));
                }
            }
        }
        
        if (!overrides.getOrDefault(slotFromName("Head"), false)) {
            if (playerSkin.headAccessory != null) {
                String[] headAccessoriesParts = playerSkin.headAccessory.split("\\.");
                var headAccessories = registry.getHeadAccessories().get(headAccessoriesParts[0]);
                if (headAccessories != null) {
                    attachments.add(ModelUtils.resolveAttachment(headAccessories, headAccessoriesParts, gradientId));
                }
            }
        }
        
        if (!overrides.getOrDefault(slotFromName("Overpants"), false)) {
            if (playerSkin.overpants != null) {
                String[] overpantsParts = playerSkin.overpants.split("\\.");
                var overpants = registry.getOverpants().get(overpantsParts[0]);
                if (overpants != null) {
                    attachments.add(ModelUtils.resolveAttachment(overpants, overpantsParts, gradientId));
                }
            }
        }
        
        if (!overrides.getOrDefault(slotFromName("Overtops"), false)) {
            if (playerSkin.overtop != null) {
                String[] overtopsParts = playerSkin.overtop.split("\\.");
                var overtops = registry.getOvertops().get(overtopsParts[0]);
                if (overtops != null) {
                    attachments.add(ModelUtils.resolveAttachment(overtops, overtopsParts, gradientId));
                }
            }
        }
        
        if (!overrides.getOrDefault(slotFromName("Pants"), false)) {
            if (playerSkin.pants != null) {
                String[] pantsParts = playerSkin.pants.split("\\.");
                var pants = registry.getPants().get(pantsParts[0]);
                if (pants != null) {
                    attachments.add(ModelUtils.resolveAttachment(pants, pantsParts, gradientId));
                }
            }
        }
        
        if (!overrides.getOrDefault(slotFromName("Shoes"), false)) {
            if (playerSkin.shoes != null) {
                String[] shoesParts = playerSkin.shoes.split("\\.");
                var shoes = registry.getShoes().get(shoesParts[0]);
                if (shoes != null) {
                    attachments.add(ModelUtils.resolveAttachment(shoes, shoesParts, gradientId));
                }
            }
        }
        
        if (!overrides.getOrDefault(slotFromName("Undertops"), false)) {
            if (playerSkin.undertop != null) {
                String[] undertopsParts = playerSkin.undertop.split("\\.");
                var undertops = registry.getUndertops().get(undertopsParts[0]);
                if (undertops != null) {
                    attachments.add(ModelUtils.resolveAttachment(undertops, undertopsParts, gradientId));
                }
            }
        }
        
        if (!overrides.getOrDefault(slotFromName("Underwears"), false)) {
            if (playerSkin.underwear != null) {
                String[] underwearParts = playerSkin.underwear.split("\\.");
                var underwear = registry.getUnderwear().get(underwearParts[0]);
                if (underwear != null) {
                    attachments.add(ModelUtils.resolveAttachment(underwear, underwearParts, gradientId));
                }
            }
        }
        
        if (!overrides.getOrDefault(slotFromName("Ears_Accessories"), false)) {
            if (playerSkin.earAccessory != null) {
                String[] earAccessoriesParts = playerSkin.earAccessory.split("\\.");
                var earAccessories = registry.getEarAccessories().get(earAccessoriesParts[0]);
                if (earAccessories != null) {
                    attachments.add(ModelUtils.resolveAttachment(earAccessories, earAccessoriesParts, gradientId));
                }
            }
        }
        
        if (playerSkin.skinFeature != null) {
            String[] skinFeaturesParts = playerSkin.skinFeature.split("\\.");
            var skinFeatures = registry.getSkinFeatures().get(skinFeaturesParts[0]);
            if (skinFeatures != null) {
                attachments.add(ModelUtils.resolveAttachment(skinFeatures, skinFeaturesParts, gradientId));
            }
        }
    }
    
    // Checks if a player has marked a specific slot as empty.
    public boolean isEmptySlot(Ref<EntityStore> ref, Slot slot) {
        return containsChange(ref, "No" + slot.name);
    }
    
    // Checks if a player has a specific cosmetic or one of its variants equipped.
    public boolean containsChange(Ref<EntityStore> ref, String cosmeticId) {
        Store<EntityStore> store = ref.getStore();
        CosmeticData data = store.getComponent(ref, CosmeticData.INSTANCE);
        
        if (data == null) return false;
        
        for (String cosmetic : data.getCosmetics()) {
            if (cosmetic.equals(cosmeticId)) return true;
            if (cosmetic.startsWith(cosmeticId + "$")) return true;
            if (cosmetic.startsWith(cosmeticId + "#")) return true;
            if (cosmetic.startsWith(cosmeticId + "%")) return true;
        }
        
        return false;
    }
    
    // Gets the name of the equipped variant for a given base cosmetic ID.
    // @return The variant name, or null if no variant is equipped.
    public String getEquippedVariant(Ref<EntityStore> ref, String cosmeticId) {
        Store<EntityStore> store = ref.getStore();
        CosmeticData data = store.getComponent(ref, CosmeticData.INSTANCE);
        
        if (data == null) return null;
        
        for (String cosmetic : data.getCosmetics()) {
            if (cosmetic.startsWith(cosmeticId + "$")) {
                return cosmetic.split("\\$")[1];
            }
        }
        
        return null;
    }
    
    // Checks if a player has a specific cosmetic ID (including variant) equipped.
    public boolean isEquipped(Ref<EntityStore> ref, String cosmeticId) {
        Store<EntityStore> store = ref.getStore();
        CosmeticData data = store.getComponent(ref, CosmeticData.INSTANCE);
        
        if (data == null) return false;
        
        return data.getCosmetics().contains(cosmeticId);
    }
    
    // Removes a cosmetic (and any of its variants) from a player.
    public void removeCosmetic(Ref<EntityStore> ref, String cosmeticId) {
        removeCosmetic(ref, cosmeticId, false);
    }
    
    private boolean isSlotUsed(CosmeticData data, Slot slot) {
        for (String c : data.getCosmetics()) {
            if (c.startsWith("No")) continue;
            
            String cBase = c.split("\\$")[0];
            if (cBase.contains("%")) {
                cBase = cBase.split("%")[0];
            }
            
            Attachment att = attachmentsRegistry.get(cBase);
            if (att != null) {
                if (att.data().slot() == slot) return true;
                if (att.data().slotOverrides().contains(slot.name)) return true;
            }
        }
        return false;
    }
    
    public void removeCosmetic(Ref<EntityStore> ref, String cosmeticId, boolean multiSelect) {
        Store<EntityStore> store = ref.getStore();
        CosmeticData data = store.getComponent(ref, CosmeticData.INSTANCE);
        
        if (data == null) return;
        
        String baseId = cosmeticId.split("\\$")[0];
        if (baseId.contains("%")) {
            baseId = baseId.split("%")[0];
        }
        Attachment attachment = attachmentsRegistry.get(baseId);
        
        // Find all cosmetics to remove (base ID and any variants).
        List<String> toRemove = new ArrayList<>();
        
        for (String cosmetic : data.getCosmetics()) {
            if (cosmetic.equals(cosmeticId) || cosmetic.startsWith(cosmeticId + "$") || cosmetic.startsWith(cosmeticId + "%")) {
                toRemove.add(cosmetic);
            }
        }
        
        for (String s : toRemove) {
            data.removeCosmetic(s);
        }
        
        if (cosmeticId.startsWith("No")) {
            Slot slot = slotFromName(cosmeticId.replace("No", ""));
            if (slot != null && !multiSelect) {
                List<String> dependentCosmetics = new ArrayList<>();
                for (String cosmetic : data.getCosmetics()) {
                    if (cosmetic.startsWith("No")) continue;
                    
                    String id = cosmetic.split("\\$")[0];
                    if (id.contains("%")) {
                        id = id.split("%")[0];
                    }
                    Attachment att = attachmentsRegistry.get(id);
                    if (att != null) {
                        if (att.data().slotOverrides().contains(slot.name) || att.data().slot() == slot) {
                            dependentCosmetics.add(cosmetic);
                        }
                    }
                }
                
                for (String dep : dependentCosmetics) {
                    removeCosmetic(ref, dep, false);
                }
            }
        } else if (attachment != null && !multiSelect) {
            Set<String> slotsToCheck = new HashSet<>();
            if (attachment.data().slot() != null) {
                slotsToCheck.add(attachment.data().slot().name);
            }
            slotsToCheck.addAll(attachment.data().slotOverrides());
            
            for (String slotName : slotsToCheck) {
                Slot s = slotFromName(slotName);
                if (s != null && !isSlotUsed(data, s)) {
                    data.removeCosmetic("No" + slotName);
                }
            }
        }
        
        rebuildSkinWithCosmetics(ref);
    }
    
    // Adds a cosmetic to a player.
    // @param multiSelect If true, does not clear the slot and ignores slot overrides.
    public void addCosmetic(Ref<EntityStore> ref, String cosmeticId, boolean multiSelect) {
        String cosmId = cosmeticId;
        
        if (cosmeticId.contains("$")) {
            String[] split = cosmeticId.split("\\$");
            cosmId = split[0];
        }
        
        if (cosmeticId.contains("%")) {
            String[] split = cosmeticId.split("%");
            cosmId = split[0];
        }
        
        // Fix for Issue 3: Remove any existing version of this cosmetic (base, variant, or gradient)
        // This ensures we don't retain old colors/variants if we are equipping the base one.
        removeCosmetic(ref, cosmId, true);
        
        Store<EntityStore> store = ref.getStore();
        CosmeticData data = store.getComponent(ref, CosmeticData.INSTANCE);
        
        if (data == null) return;
        
        Attachment attachment = attachmentsRegistry.get(cosmId);
        Slot slot = attachment != null ? attachment.data().slot() : null;
        Colour defColor = attachment != null ? attachment.data().defaultColor() : null;
        
        if (slot == null && cosmId.startsWith("No")) {
            try {
                slot = slotFromName(cosmId.replace("No", ""));
            } catch (Exception _) {
            }
        }
        
        boolean isNonOverriding = slot != null && nonOverridingSlots.contains(slot);
        boolean shouldClear = !multiSelect && !isNonOverriding;
        
        if (shouldClear) {
            if (slot != null) {
                clearSlot(ref, slot);
                if (!cosmId.startsWith("No")) {
                    data.addCosmetic("No" + slot.name);
                }
            } else {
                clearSlot(ref, cosmId);
            }
        } else {
            if (slot != null && !cosmId.startsWith("No")) {
                data.removeCosmetic("No" + slot.name);
            }
        }
        
        // Add any necessary slot overrides for this cosmetic.
        if (attachment != null && !multiSelect) {
            for (String overrideSlot : attachment.data().slotOverrides()) {
                clearSlot(ref, slotFromName(overrideSlot));
                data.addCosmetic("No" + overrideSlot);
            }
        }
        
        if (defColor != null && !cosmeticId.contains("%") && !cosmeticId.contains("$"))
            cosmeticId = defColor.apply(cosmeticId);
        
        data.addCosmetic(cosmeticId);
        rebuildSkinWithCosmetics(ref);
    }
    
    // Clears all cosmetics from a given slot, determined by a cosmetic ID.
    public void clearSlot(Ref<EntityStore> ref, String cosmeticId) {
        String id = cosmeticId;
        
        if (id.contains("$")) {
            id = id.split("\\$")[0];
        }
        if (id.contains("%")) {
            id = id.split("%")[0];
        }
        
        if (cosmeticId.contains("No")) {
            clearSlot(ref, slotFromName(cosmeticId.replace("No", "")));
            return;
        }
        
        Attachment attachment = attachmentsRegistry.get(id);
        
        if (attachment == null) return;
        
        clearSlot(ref, attachment.data().slot());
    }
    
    // Clears all cosmetics from a specific slot.
    public void clearSlot(Ref<EntityStore> ref, Slot slot) {
        Store<EntityStore> store = ref.getStore();
        CosmeticData data = store.getComponent(ref, CosmeticData.INSTANCE);
        
        if (data == null) return;
        
        List<String> toRemove = new ArrayList<>();
        
        // Find all cosmetics belonging to the specified slot.
        for (String cosmetic : data.getCosmetics()) {
            if (cosmetic.equals("No" + slot.name)) {
                toRemove.add(cosmetic);
                continue;
            }
            
            String id = cosmetic;
            if (id.contains("$")) {
                id = id.split("\\$")[0];
            }
            if (id.contains("%")) {
                id = id.split("%")[0];
            }
            
            Attachment attachment = attachmentsRegistry.get(id);
            
            if (attachment == null) {
                if (cosmetic.startsWith("No")) {
                    continue;
                }
                
                // Remove invalid cosmetics.
                toRemove.add(cosmetic);
                continue;
            }
            
            if (attachment.data().slot() == slot) {
                toRemove.add(cosmetic);
            }
        }
        
        // Remove them.
        for (String rev : toRemove) {
            removeCosmetic(ref, rev);
        }
        
        rebuildSkinWithCosmetics(ref);
    }
    
    // Clears all custom attachments (both cosmetics and character parts) from the player.
    public void clearAll(Ref<EntityStore> ref) {
        for (Slot slot : slots) {
            clearSlot(ref, slot);
        }
    }
    
    // Registers a new attachment with default paths but with specified variants.
    public void registerJsonLess(String name, Slot slot, String attachmentPath, Map<String, Variant> variants) {
        String[] split = name.split("#");
        
        // Create AttachmentData with conventional paths.
        AttachmentData attData = new AttachmentData(
            String.format("%s/%s.blockymodel", attachmentPath, split[1]),
            String.format("%s/%s.png", attachmentPath, split[1]),
            String.format("%s/Icon/%s.png", attachmentPath, split[1]),
            variants,
            "",
            new ArrayList<>(),
            new Colour("", "")
        );
        attData.slot = slot;
        
        registerJson(name, attData);
    }
    
    public void registerJsonLess(String name, Slot slot, String attachmentPath) {
        registerJsonLess(name, slot, attachmentPath, Map.of());
    }
    
    // Registers a new attachment with default paths but with specified variants.
    public void registerJsonLess(String name, Slot slot, String attachmentPath, String gradientSet) {
        String[] split = name.split("#");
        
        // Create AttachmentData with conventional paths.
        AttachmentData attData = new AttachmentData(
            String.format("%s/%s.blockymodel", attachmentPath, split[1]),
            String.format("%s/%s.png", attachmentPath, split[1]),
            String.format("%s/Icon/%s.png", attachmentPath, split[1]),
            Map.of(),
            gradientSet,
            List.of(),
            new Colour("", "")
        );
        attData.slot = slot;
        
        registerJson(name, attData);
    }
    
    // The core registration method. Adds a fully-formed AttachmentData object to the registry.
    public void registerJson(String name, AttachmentData attachmentData) {
        if (attachmentData.slot == null) {
            CosmeticCore.log(String.format("Failed to register %s: Slot is null", name));
            return;
        }
        CosmeticCore.log(String.format("Registered        Name: %s        Slot: %s", name, attachmentData.slot.name));// Dev
        attachmentsRegistry.put(name, new Attachment(name, attachmentData));
    }
    
    // Utility method to find the key for a given Attachment object.
    public String getKey(Attachment e) {
        for (Map.Entry<String, Attachment> entry : attachmentsRegistry.entrySet()) {
            if (entry.getValue() == e) {
                return entry.getKey();
            }
        }
        
        return null;
    }
    
    // Clears all attachments from the registry. Used during reload.
    public void clear() {
        attachmentsRegistry.clear();
        topLevelCategories.clear();
        slots.clear();
        nonOverridingSlots.clear();
        connections.clear();
    }
    
    // Returns a sorted list of all registered cosmetic IDs.
    public List<String> getAttachmentsList() {
        return attachmentsRegistry.keySet().stream().sorted().collect(Collectors.toList());
    }
    
}