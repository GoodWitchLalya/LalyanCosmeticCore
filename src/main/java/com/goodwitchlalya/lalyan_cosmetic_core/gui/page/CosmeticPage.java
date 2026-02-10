package com.goodwitchlalya.lalyan_cosmetic_core.gui.page;

import com.goodwitchlalya.lalyan_cosmetic_core.CosmeticCore;
import com.goodwitchlalya.lalyan_cosmetic_core.util.AttachmentsRegistry;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.*;
import com.hypixel.hytale.protocol.packets.camera.SetServerCamera;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.*;

/**
 * Represents the main interactive UI page for cosmetic customization.
 * This class is responsible for building the UI, handling player interactions,
 * and updating the player's appearance based on their selections.
 */
public class CosmeticPage extends InteractiveCustomUIPage<CosmeticPage.Data> {
    
    /**
     * Constructor for the CosmeticPage.
     *
     * @param playerRef A reference to the player for whom this page is being created.
     */
    public CosmeticPage(@NonNullDecl PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss, Data.CODEC);
    }
    
    private static final Map<String, String> colorCodes = new HashMap<>();
    
    // The currently selected cosmetic sub-category (e.g., Haircuts, Capes).
    private AttachmentsRegistry.TopLevelCategory tlc = AttachmentsRegistry.get().getTopLevelCategories().getFirst();
    private AttachmentsRegistry.Slot currentSlot = AttachmentsRegistry.get().slotFromTopLevelCategory(tlc).getFirst();
    
    // The current search term entered by the player.
    private String search;
    
    // The ID of the cosmetic for which variants are currently being displayed.
    private String originalId;
    // A map of variants for the currently selected cosmetic.
    private Map<String, AttachmentsRegistry.Variant> variants = new HashMap<>();
    
    //Whether to allow multiple cosmetics of the same type regardless of slot
    private boolean singleSelect = true;
    
    private String gradientSet;
    
    /**
     * Builds the main UI structure. This method is called to generate the UI commands
     * that create the visual elements on the player's screen.
     */
    @Override
    public void build(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl UICommandBuilder cmd, @NonNullDecl UIEventBuilder evt, @NonNullDecl Store<EntityStore> store) {
        // Load the base UI layout from a file.
        cmd.append("Pages/CosmeticGUI/LCC_CosmeticPage.ui");
        
        // If there's an active search, set the value of the search input field.
        if (this.search != null) {
            cmd.set("#SearchInput.Value", this.search);
        }
        
        // Build the dynamic parts of the UI.
        buildTopCategoryButtons(cmd, evt);
        buildCategoryButtons(cmd, evt);
        buildCosmetics(ref, cmd, evt);
        
        cmd.set("#Title #MultiSelect #CheckBox.Value", !this.singleSelect);
        
        evt.addEventBinding(CustomUIEventBindingType.ValueChanged, "#Title #MultiSelect #CheckBox", EventData.of("MultiSelect", "true"));
        
        updateCamera();
    }
    
    private void buildTopCategoryButtons(UICommandBuilder cmd, UIEventBuilder evt) {
        cmd.clear("#TopCategoryPanel #Content");
        cmd.clear("#CategoryPanel #Content");
        
        for (AttachmentsRegistry.TopLevelCategory tlc : AttachmentsRegistry.get().getTopLevelCategories()) {
            String selector = "#TLC" + tlc.name;
            String contentSel = "#TopCategoryPanel #Content " + selector;
            
            cmd.appendInline("#TopCategoryPanel #Content", "Group " + selector + " {}");
            cmd.append(contentSel, "Pages/CosmeticGUI/TopCategoryEntry.ui");
            
            cmd.set(contentSel + " #Image.AssetPath", "UI/Custom/Common/Categories/Top/" + tlc.name + ".png");
            cmd.set(contentSel + " #ImageSelected.AssetPath", "UI/Custom/Common/Categories/Top/Selected/" + tlc.name + ".png");
            
            cmd.set(contentSel + " #TopCategoryButton.TooltipText", tlc.name);
            
            evt.addEventBinding(CustomUIEventBindingType.Activating, contentSel + " #TopCategoryButton", EventData.of("TLC", tlc.name));
            
            if (this.tlc != tlc) {
                cmd.set(contentSel + " #Image.Visible", true);
                cmd.set(contentSel + " #ImageSelected.Visible", false);
                continue;
            }
            
            cmd.set(contentSel + " #Image.Visible", false);
            cmd.set(contentSel + " #ImageSelected.Visible", true);
        }
    }
    
    private void buildCategoryButtons(UICommandBuilder cmd, UIEventBuilder evt) {
        if (this.tlc == null) return;
        List<AttachmentsRegistry.Slot> slots = AttachmentsRegistry.get().slotFromTopLevelCategory(this.tlc);
        
        for (AttachmentsRegistry.Slot slot : slots) {
            
            if (CosmeticCore.getSlotPerm(playerRef, slot)) continue;

            String selector = "#C" + slot.name;
            String contentSel = "#CategoryPanel #Content " + selector;
            
            cmd.appendInline("#CategoryPanel #Content", "Group " + selector + " {}");
            cmd.append(contentSel, "Pages/CosmeticGUI/CategoryEntry.ui");
            
            cmd.set(contentSel + " #Image.AssetPath", slot.icon);
            cmd.set(contentSel + " #ImageSelected.AssetPath", slot.selectedIcon);
            
            cmd.set(contentSel + " #CategoryButton.TooltipText", slot.name);
            
            evt.addEventBinding(CustomUIEventBindingType.Activating, contentSel + " #CategoryButton", EventData.of("Slot", slot.name));
            
            if (this.currentSlot != slot) {
                cmd.set(contentSel + " #Image.Visible", true);
                cmd.set(contentSel + " #ImageSelected.Visible", false);
                continue;
            }
            
            cmd.set(contentSel + " #Image.Visible", false);
            cmd.set(contentSel + " #ImageSelected.Visible", true);
        }
    }
    
    /**
     * Builds the main grid of cosmetic items based on the current filters.
     */
    private void buildCosmetics(Ref<EntityStore> ref, UICommandBuilder cmd, UIEventBuilder evt) {
        Map<String, AttachmentsRegistry.Attachment> registry = AttachmentsRegistry.get().getAttachmentsRegistry();
        
        int itemsPerRow = 8;
        
        cmd.clear("#Content #CosmeticGrid");
        
        cmd.appendInline("#Content #CosmeticGrid", "Group #CosmeticRow { LayoutMode: Top; }");
        
        // Filter and sort the attachments from the registry.
        AttachmentsRegistry.Attachment[] entries = registry.values()
            .stream()
            // Filter by the currently selected slot.
            .filter(a -> {
                if (this.tlc.name.equals("All")) return true;
                return a.data().slot() == currentSlot;
            })
            // Filter by the search term.
            .filter(a -> {
                if (this.search == null || this.search.isEmpty()) return true;
                return a.name().toLowerCase().contains(this.search.toLowerCase());
            })
            .filter( a -> {
                return CosmeticCore.getCosmeticPerm(playerRef, a);
            })
            // Sort the items for a consistent display order.
            .sorted(Comparator.comparing(a -> {
                String name = a.name();
                
                String[] authorSplit = name.split(":");
                String[] assetPackSplit = authorSplit[1].split("#");
                String[] cosmeticNameSplit = assetPackSplit[1].split("\\$");
                
                return cosmeticNameSplit[0];
            }))
            .toList()
            .toArray(new AttachmentsRegistry.Attachment[0]);
        
        // Get the unique keys for the filtered entries.
        String[] keys = Arrays.stream(entries)
            .map(e -> AttachmentsRegistry.get().getKey(e))
            .filter(Objects::nonNull)
            .toArray(String[]::new);
        
        // Determine if the "unequip" (vanish) button should be shown.
        boolean showVanish = currentSlot != null && currentSlot.canVanish;
        int totalItems = entries.length + (showVanish ? 1 : 0);
        
        // Loop to create the grid of items.
        for (int i = 0; i < totalItems; i++) {
            int rowIndex = i / itemsPerRow;
            int colIndex = i % itemsPerRow;
            
            if (colIndex == 0) {
                cmd.append("#CosmeticRow", "Pages/CosmeticGUI/CosmeticRow.ui");
            }
            
            String rowSelector = "#CosmeticRow[" + rowIndex + "]";
            cmd.append(rowSelector + " #CosmeticSlot", "Pages/CosmeticGUI/CosmeticSlot.ui");
            
            String slotSelector = rowSelector + " #CosmeticSlot[" + colIndex + "]";
            
            // Special handling for the first item if it's the "vanish" button.
            if (showVanish && i == 0) {
                cmd.set(slotSelector + " #Icon.AssetPath", "UI/Custom/Common/Categories/VanishPart.png");
                
                if (AttachmentsRegistry.get().isEmptySlot(ref, currentSlot)) {
                    cmd.set(slotSelector + " #Button.Visible", false);
                    cmd.set(slotSelector + " #ButtonEnabled.Visible", true);
                }
                
                evt.addEventBinding(CustomUIEventBindingType.Activating, slotSelector + " #Button", EventData.of("CosmeticId", "No" + currentSlot.name).append("Enabled", "false"));
                evt.addEventBinding(CustomUIEventBindingType.Activating, slotSelector + " #ButtonEnabled", EventData.of("CosmeticId", "No" + currentSlot.name).append("Enabled", "true"));
                cmd.set(slotSelector + " #VariantIcon.Visible", false);
                
                continue;
            }
            // Regular cosmetic item.
            int entryIndex = showVanish ? i - 1 : i;
            AttachmentsRegistry.Attachment entry = entries[entryIndex];
            String cosmeticId = keys[entryIndex];
            
            String icon = entry.data().icon();
            
            // Check if a variant is equipped and update the icon accordingly.
            String equippedVariant = AttachmentsRegistry.get().getEquippedVariant(ref, cosmeticId);
            if (equippedVariant != null) {
                AttachmentsRegistry.Variant eVar = entry.data().variants().get(equippedVariant);
                if(eVar.icon != null) icon = eVar.icon();
            }
            
            if (icon == null) continue;
            
            cmd.set(slotSelector + " #Icon.AssetPath", icon);
            
            // Build and set the tooltip text with cosmetic details.
            String name = entry.name();
            
            String[] authorSplit = name.split(":");
            
            String author = "Author: " + authorSplit[0];
            
            String[] assetPackSplit = authorSplit[1].split("#");
            
            String assetPackName = "AssetPack: " + assetPackSplit[0];
            
            String[] cosmeticNameSplit = assetPackSplit[1].split("\\$");
            
            String cosmeticName = "Name: " + cosmeticNameSplit[0];
            
            cmd.set(slotSelector + " #Button.TooltipText", cosmeticName + "\n" + author + "\n" + assetPackName);
            cmd.set(slotSelector + " #ButtonEnabled.TooltipText", cosmeticName + "\n" + author + "\n" + assetPackName);
            
            // Set the button state based on whether the cosmetic is equipped.
            if (AttachmentsRegistry.get().containsChange(ref, cosmeticId)) {
                cmd.set(slotSelector + " #Button.Visible", false);
                cmd.set(slotSelector + " #ButtonEnabled.Visible", true);
            }
            
            // Bind click events for equipping/unequipping.
            evt.addEventBinding(CustomUIEventBindingType.Activating, slotSelector + " #Button", EventData.of("CosmeticId", cosmeticId).append("Enabled", "false"));
            evt.addEventBinding(CustomUIEventBindingType.Activating, slotSelector + " #ButtonEnabled", EventData.of("CosmeticId", cosmeticId).append("Enabled", "true"));
            
            String gradientSet = entry.data().gradientSet();
            
            if (gradientSet != null && !gradientSet.isEmpty()) {
                cmd.set(slotSelector + " #VariantIcon.Visible", true);
                
                String cosmetic = name + "%" + gradientSet;
                
                evt.addEventBinding(CustomUIEventBindingType.RightClicking, slotSelector + " #Button", EventData.of("GradientId", cosmetic));
                evt.addEventBinding(CustomUIEventBindingType.RightClicking, slotSelector + " #ButtonEnabled", EventData.of("GradientId", cosmetic));
                
                continue;
            }
            
            // Handle variants.
            Map<String, AttachmentsRegistry.Variant> variants = entry.data().variants();
            
            boolean hasVariants = variants != null && !variants.isEmpty();
            cmd.set(slotSelector + " #VariantIcon.Visible", hasVariants);
            
            if (!hasVariants) continue;
            
            // Bind right-click events to open the variant selection panel.
            evt.addEventBinding(CustomUIEventBindingType.RightClicking, slotSelector + " #Button", EventData.of("VariantId", cosmeticId));
            evt.addEventBinding(CustomUIEventBindingType.RightClicking, slotSelector + " #ButtonEnabled", EventData.of("VariantId", cosmeticId));
        }
        
        // Bind an event to the search input field.
        evt.addEventBinding(CustomUIEventBindingType.ValueChanged, "#SearchInput", EventData.of("@Search", "#SearchInput.Value"), false);
        
        // --- Build Variant Panel ---
        if (buildVariants(ref, cmd, evt)) return;
        
        if (this.gradientSet == null || this.gradientSet.isEmpty()) return;
        
        cmd.set("#RSidePanel.Visible", true);
        
        AttachmentsRegistry.GradientSet set = AttachmentsRegistry.coloursDataSet.getGradientSet(this.gradientSet.split("%")[1]);
        
        List<String> colourList = set.colourList();
        for (int i = 0; i < colourList.size(); i++) {
            String color = colourList.get(i);
            
            String selector = "#VariantSlot[" + (i) + "]";
            
            cmd.append("#RSidePanel #Content #VariantList #VariantSlot", "Pages/CosmeticGUI/ColorSlot.ui");
            
            cmd.set("#RSidePanel #Content #VariantList " + selector + " #Button #Colors.Background", colorCodes.get(color));
            
            cmd.set("#RSidePanel #Content #VariantList " + selector + " #Button.TooltipText", color);
            
            String cosmId = this.originalId + ":" + color;
            
            boolean isEquipped = AttachmentsRegistry.get().isEquipped(ref, cosmId);
            cmd.set("#RSidePanel #Content #VariantList " + selector + " #Button #SelectedHighlight.Visible", isEquipped);
            
            evt.addEventBinding(CustomUIEventBindingType.Activating, "#RSidePanel #Content #VariantList " + selector + " #Button", EventData.of("CosmeticId", cosmId).append("Enabled", String.valueOf(isEquipped)));
        }
    }
    
    private boolean buildVariants(Ref<EntityStore> ref, UICommandBuilder cmd, UIEventBuilder evt) {
        if (variants == null || variants.isEmpty()) {
            cmd.set("#RSidePanel.Visible", false);
            return false;
        }
        
        cmd.clear("#RSidePanel #Content #VariantList #VariantSlot");
        
        cmd.set("#RSidePanel.Visible", true);
        
        // Add the "default" variant button.
        cmd.append("#RSidePanel #Content #VariantList #VariantSlot", "Pages/CosmeticGUI/VariantSlot.ui");
        
        AttachmentsRegistry.Attachment original = AttachmentsRegistry.get().getAttachmentsRegistry().get(originalId);
        
        cmd.set("#RSidePanel #Content #VariantList #VariantSlot[0] #Icon.AssetPath", original.data().icon());
        
        cmd.set("#RSidePanel #Content #VariantList #VariantSlot[0] #Button.TooltipText", "Default");
        cmd.set("#RSidePanel #Content #VariantList #VariantSlot[0] #ButtonEnabled.TooltipText", "Default");
        
        if (AttachmentsRegistry.get().isEquipped(ref, this.originalId)) {
            cmd.set("#RSidePanel #Content #VariantList #VariantSlot[0] #Button.Visible", false);
            cmd.set("#RSidePanel #Content #VariantList #VariantSlot[0] #ButtonEnabled.Visible", true);
        } else {
            cmd.set("#RSidePanel #Content #VariantList #VariantSlot[0] #Button.Visible", true);
            cmd.set("#RSidePanel #Content #VariantList #VariantSlot[0] #ButtonEnabled.Visible", false);
        }
        
        evt.addEventBinding(CustomUIEventBindingType.Activating, "#RSidePanel #Content #VariantList #VariantSlot[0] #Button", EventData.of("CosmeticId", this.originalId).append("Enabled", "false"));
        evt.addEventBinding(CustomUIEventBindingType.Activating, "#RSidePanel #Content #VariantList #VariantSlot[0] #ButtonEnabled", EventData.of("CosmeticId", this.originalId).append("Enabled", "true"));
        
        // Add buttons for each available variant.
        List<Map.Entry<String, AttachmentsRegistry.Variant>> usableVariants = variants.entrySet().stream().toList();
        for (int i = 0; i < usableVariants.size(); i++) {
            Map.Entry<String, AttachmentsRegistry.Variant> entry = usableVariants.get(i);
            
            String selector = "#VariantSlot[" + (i + 1) + "]";
            
            cmd.append("#RSidePanel #Content #VariantList #VariantSlot", "Pages/CosmeticGUI/VariantSlot.ui");
            
            String vIcon = entry.getValue().icon();
            cmd.set("#RSidePanel #Content #VariantList " + selector + " #Icon.AssetPath", vIcon != null? vIcon: original.data().icon());
            
            cmd.set("#RSidePanel #Content #VariantList " + selector + " #Button.TooltipText", entry.getKey());
            cmd.set("#RSidePanel #Content #VariantList " + selector + " #ButtonEnabled.TooltipText", entry.getKey());
            
            String variantId = this.originalId + "$" + entry.getKey();
            
            if (AttachmentsRegistry.get().isEquipped(ref, variantId)) {
                cmd.set("#RSidePanel #Content #VariantList " + selector + " #Button.Visible", false);
                cmd.set("#RSidePanel #Content #VariantList " + selector + " #ButtonEnabled.Visible", true);
            } else {
                cmd.set("#RSidePanel #Content #VariantList " + selector + " #Button.Visible", true);
                cmd.set("#RSidePanel #Content #VariantList " + selector + " #ButtonEnabled.Visible", false);
            }
            
            evt.addEventBinding(CustomUIEventBindingType.Activating, "#RSidePanel #Content #VariantList " + selector + " #Button", EventData.of("CosmeticId", variantId).append("Enabled", "false"));
            evt.addEventBinding(CustomUIEventBindingType.Activating, "#RSidePanel #Content #VariantList " + selector + " #ButtonEnabled", EventData.of("CosmeticId", variantId).append("Enabled", "true"));
        }
        
        return true;
    }
    
    /**
     * Updates the camera settings based on the currently selected slot.
     */
    private void updateCamera() {
        Vector3f headRot = this.playerRef.getHeadRotation();
        
        ServerCameraSettings settings = new ServerCameraSettings();
        
        settings.distance = 2;
        settings.positionLerpSpeed = 0.1f;
        settings.rotationLerpSpeed = 0.1f;
        settings.displayCursor = true;
        settings.isFirstPerson = false;
        settings.mouseInputTargetType = MouseInputTargetType.None;
        settings.sendMouseMotion = false;
        settings.mouseInputType = MouseInputType.LookAtPlane;
        settings.rotationType = RotationType.Custom;
        settings.eyeOffset = true;
        settings.rotation = new Direction((float) (headRot.getYaw() + Math.PI), headRot.getPitch(), headRot.getRoll());
        settings.allowPitchControls = false;
        settings.planeNormal = new com.hypixel.hytale.protocol.Vector3f(0, 0, 0);
        settings.positionOffset = new Position(0, -0.3, 0);
        
        if (currentSlot != null) {
            AttachmentsRegistry.SlotCameraProperties camera = currentSlot.camera;
            
            if (camera != null) {
                settings.distance = camera.distance;
                settings.positionOffset = camera.positionOffset;
                settings.rotation = camera.rotation;
                
                if (camera.lookAtBack) {
                    settings.rotation = new Direction(headRot.getYaw(), headRot.getPitch(), headRot.getRoll());
                }
            }
        }
        
        this.playerRef.getPacketHandler().writeNoCache(new SetServerCamera(ClientCameraView.Custom, true, settings));
    }
    
    /**
     * Handles incoming data events from the UI, such as button clicks or input changes.
     */
    @Override
    public void handleDataEvent(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl Store<EntityStore> store, @NonNullDecl Data data) {
        super.handleDataEvent(ref, store, data);
        
        if (data.multiSelect != null) {
            this.singleSelect = !this.singleSelect;
            
            this.sendUpdate();
            this.rebuild();
            return;
        }
        
        // Handle search input changes.
        if (data.search != null) {
            this.search = data.search;
            
            UICommandBuilder commandBuilder = new UICommandBuilder();
            UIEventBuilder eventBuilder = new UIEventBuilder();
            
            this.buildCosmetics(ref, commandBuilder, eventBuilder);
            this.sendUpdate(commandBuilder, eventBuilder, false);
            
            return;
        }
        
        // Handle right-click to open variant panel.
        if (data.variantId != null) {
            this.gradientSet = null;
            this.variants = new HashMap<>();
            AttachmentsRegistry.Attachment attachment = AttachmentsRegistry.get().getAttachmentsRegistry().get(data.variantId);
            this.variants = attachment.data().variants();
            this.originalId = data.variantId;
            
            this.sendUpdate();
            this.rebuild();
            
            return;
        }
        
        if (data.gradientId != null) {
            this.gradientSet = data.gradientId;
            this.originalId = data.gradientId;
            
            this.sendUpdate();
            this.rebuild();
            
            return;
        }
        
        // Handle top-level category selection.
        if (data.tlc != null) {
            this.gradientSet = null;
            this.variants = new HashMap<>();
            this.originalId = null;
            this.search = null;
            this.tlc = AttachmentsRegistry.get().tlcFromName(data.tlc);
            
            this.currentSlot = AttachmentsRegistry.get().slotFromTopLevelCategory(this.tlc).getFirst();
            
            updateCamera();
            this.sendUpdate();
            this.rebuild();
            
            return;
        }
        
        // Handle sub-category selection.
        if (data.slot != null) {
            this.gradientSet = null;
            this.variants = new HashMap<>();
            this.originalId = null;
            this.search = null;
            if (data.slot.equals("All")) {
                this.currentSlot = null;
            } else {
                this.currentSlot = AttachmentsRegistry.get().slotFromName(data.slot);
            }
            updateCamera();
            this.sendUpdate();
            this.rebuild();
            
            return;
        }
        
        // --- Handle Cosmetic/Variant Clicks ---
        
        String baseId = data.cosmeticId;
        if (baseId != null && baseId.contains("$")) {
            baseId = baseId.split("\\$")[0];
        }
        
        if (baseId != null && baseId.contains("%")) {
            baseId = baseId.split("%")[0];
        }
        
        String testId = this.originalId;
        if (testId != null && testId.contains("$")) {
            testId = testId.split("\\$")[0];
        }
        
        if (testId != null && testId.contains("%")) {
            testId = testId.split("%")[0];
        }
        
        // If the player clicks a different cosmetic, close the variant panel.
        if (!Objects.equals(baseId, testId)) {
            this.gradientSet = null;
            this.variants = new HashMap<>();
            this.originalId = null;
        }
        
        // Handle un-equipping an item.
        if (data.enabled.equals("true")) {
            if (!data.cosmeticId.contains("$") && !data.cosmeticId.contains("%")) {
                this.gradientSet = null;
                this.variants = new HashMap<>();
                this.originalId = null;
            }
            
            AttachmentsRegistry.get().removeCosmetic(ref, data.cosmeticId, !singleSelect);
            
            this.sendUpdate();
            this.rebuild();
            
            return;
        }
        // Determining the slot of the cosmetic
        AttachmentsRegistry.get().addCosmetic(ref, data.cosmeticId, !singleSelect);
        
        this.sendUpdate();
        this.rebuild();
    }
    
    @Override
    public void onDismiss(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl Store<EntityStore> store) {
        super.onDismiss(ref, store);
        this.playerRef.getPacketHandler().writeNoCache(new SetServerCamera(ClientCameraView.Custom, false, null));
    }
    
    /**
     * Defines the data structure for events sent from the client-side UI.
     * The CODEC is used to serialize/deserialize this data between client and server.
     */
    public static class Data {
        private String cosmeticId;
        private String enabled;
        
        private String slot;
        
        private String tlc;
        
        private String search;
        private String variantId;
        
        private String gradientId;
        
        private String multiSelect;
        
        public static final BuilderCodec<Data> CODEC = BuilderCodec.builder(Data.class, Data::new)
            .append(new KeyedCodec<>("CosmeticId", BuilderCodec.STRING), (data, value) -> data.cosmeticId = value, (data) -> data.cosmeticId)
            .add()
            .append(new KeyedCodec<>("Enabled", BuilderCodec.STRING), (data, value) -> data.enabled = value, (data) -> data.enabled)
            .add()
            .append(new KeyedCodec<>("Slot", BuilderCodec.STRING), (data, value) -> data.slot = value, (data) -> data.slot)
            .add()
            .append(new KeyedCodec<>("TLC", BuilderCodec.STRING), (data, value) -> data.tlc = value, (data) -> data.tlc)
            .add()
            .append(new KeyedCodec<>("VariantId", BuilderCodec.STRING), (data, value) -> data.variantId = value, (data) -> data.variantId)
            .add()
            .append(new KeyedCodec<>("@Search", BuilderCodec.STRING), (data, value) -> data.search = value, (data) -> data.search)
            .add()
            .append(new KeyedCodec<>("MultiSelect", BuilderCodec.STRING), (data, value) -> data.multiSelect = value, (data) -> data.multiSelect)
            .add()
            .append(new KeyedCodec<>("GradientId", BuilderCodec.STRING), (data, value) -> data.gradientId = value, (data) -> data.gradientId)
            .add()
            .build();
    }
    
    static {
        colorCodes.put("Beige", "#F5F5DC");
        colorCodes.put("Black", "#1D1D21");
        colorCodes.put("Blond", "#FAE7B5");
        colorCodes.put("BlondCaramel", "#C68E17");
        colorCodes.put("BlondPlatinum", "#F0F0EC");
        colorCodes.put("BlondSand", "#DCD0BA");
        colorCodes.put("Blue", "#3C44AA");
        colorCodes.put("BlueDark", "#212663");
        colorCodes.put("BlueLight", "#8FA5D6");
        colorCodes.put("BluePastel", "#A1B4D6");
        colorCodes.put("Blue_Anthracite", "#2A303C");
        colorCodes.put("Blue_Night", "#161B40");
        colorCodes.put("Brass_Purple", "#B5A642");
        colorCodes.put("Brown", "#633C1F");
        colorCodes.put("BrownDark", "#3B2412");
        colorCodes.put("BrownDarker", "#26170B");
        colorCodes.put("BrownLight", "#8F6B4E");
        colorCodes.put("BrownSemiDark", "#4F3019");
        colorCodes.put("BrownSemiLight", "#785233");
        colorCodes.put("Bubblegum", "#FF85A2");
        colorCodes.put("Carmin", "#960018");
        colorCodes.put("Charcoal", "#36454F");
        colorCodes.put("Copper", "#B87333");
        colorCodes.put("Copper_Green", "#4CB7A5");
        colorCodes.put("Cream", "#FFFDD0");
        colorCodes.put("Gold_Red", "#D4AF37");
        colorCodes.put("Green", "#5E7C16");
        colorCodes.put("GreenLight", "#83AA2E");
        colorCodes.put("Grey", "#808080");
        colorCodes.put("GreyAsh", "#B2BEB5");
        colorCodes.put("GreyBlue", "#778899");
        colorCodes.put("GreyDark", "#404040");
        colorCodes.put("GreyLight", "#D3D3D3");
        colorCodes.put("GreyPurple", "#7D7096");
        colorCodes.put("Honey", "#D4AF37");
        colorCodes.put("Iron_Black", "#434B4D");
        colorCodes.put("Lavender", "#E6E6FA");
        colorCodes.put("Lime", "#76C610");
        colorCodes.put("Marine_Blue", "#000080");
        colorCodes.put("Maroon", "#800000");
        colorCodes.put("Orange", "#F07613");
        colorCodes.put("OrangePastel", "#FFB347");
        colorCodes.put("Orange_Tan", "#D2B48C");
        colorCodes.put("Pink", "#F28CA7");
        colorCodes.put("PinkBerry", "#990F4B");
        colorCodes.put("PinkPastel", "#FFD1DC");
        colorCodes.put("PitchBlack", "#050505");
        colorCodes.put("Purple", "#800080");
        colorCodes.put("PurplePastel", "#C3B1E1");
        colorCodes.put("Red", "#B02E26");
        colorCodes.put("RedDark", "#5E1814");
        colorCodes.put("Silver_Blue", "#B0C4DE");
        colorCodes.put("Turquoise", "#40E0D0");
        colorCodes.put("Turquoise_Dark", "#00CED1");
        colorCodes.put("Violet", "#EE82EE");
        colorCodes.put("White", "#FFFFFF");
        colorCodes.put("Yellow", "#FED83D");
        colorCodes.put("01", "#190F0D");
        colorCodes.put("02", "#31221F");
        colorCodes.put("03", "#4D272B");
        colorCodes.put("04", "#4F2A24");
        colorCodes.put("05", "#513425");
        colorCodes.put("06", "#5E3A2F");
        colorCodes.put("07", "#765E48");
        colorCodes.put("08", "#63492F");
        colorCodes.put("09", "#6F3B2C");
        colorCodes.put("10", "#6C3F40");
        colorCodes.put("11", "#7D432B");
        colorCodes.put("12", "#945D44");
        colorCodes.put("13", "#AB7A4C");
        colorCodes.put("14", "#BA7F5B");
        colorCodes.put("15", "#D98C5B");
        colorCodes.put("16", "#D5A082");
        colorCodes.put("17", "#E0AE72");
        colorCodes.put("18", "#DCC5B0");
        colorCodes.put("19", "#DCC7A8");
        colorCodes.put("20", "#F4C39A");
        colorCodes.put("21", "#F5C490");
        colorCodes.put("22", "#F5BC83");
        colorCodes.put("23", "#B22A2A");
        colorCodes.put("24", "#F06F47");
        colorCodes.put("25", "#FC8572");
        colorCodes.put("26", "#FF9C5B");
        colorCodes.put("27", "#F4C944");
        colorCodes.put("28", "#B0B283");
        colorCodes.put("29", "#D5F0A0");
        colorCodes.put("30", "#9BC55D");
        colorCodes.put("31", "#5EAE37");
        colorCodes.put("32", "#50843A");
        colorCodes.put("33", "#A0DFFF");
        colorCodes.put("34", "#8AACFB");
        colorCodes.put("35", "#3276C3");
        colorCodes.put("36", "#4354E6");
        colorCodes.put("37", "#263D50");
        colorCodes.put("38", "#6C2ABD");
        colorCodes.put("39", "#A78AF1");
        colorCodes.put("40", "#DDBFE8");
        colorCodes.put("41", "#EC6FF7");
        colorCodes.put("42", "#FF72C2");
        colorCodes.put("43", "#FF95CD");
        colorCodes.put("44", "#F0B9F2");
        colorCodes.put("45", "#F3F3F3");
        colorCodes.put("46", "#2B2B2F");
        colorCodes.put("47", "#131111");
    }
}