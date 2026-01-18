package com.goodwitchlalya.lalyan_cosmetic_core.gui.page;

import com.goodwitchlalya.lalyan_cosmetic_core.util.AttachmentsRegistry;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
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

public class CosmeticPage extends InteractiveCustomUIPage<CosmeticPage.Data> {
    public CosmeticPage(@NonNullDecl PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss, Data.CODEC);
    }
    
    private AttachmentsRegistry.Slot currentSlot = AttachmentsRegistry.CharacterSlot.Haircuts;
    private AttachmentsRegistry.TopLevelTypes tlt = AttachmentsRegistry.TopLevelTypes.Head;
    
    private String variantOriginalId;
    private Map<String, AttachmentsRegistry.Variant> variants = new HashMap<>();
    
    @Override
    public void build(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl UICommandBuilder cmd, @NonNullDecl UIEventBuilder evt, @NonNullDecl Store<EntityStore> store) {
        cmd.append("Pages/CosmeticGUI/LCC_CosmeticPage.ui");
        
        buildCosmeticButtons(cmd, evt);
        buildCosmetics(ref, cmd, evt);
    }
    
    private void buildCosmeticButtons(UICommandBuilder cmd, UIEventBuilder evt) {
        for (AttachmentsRegistry.TopLevelTypes tlt : AttachmentsRegistry.TopLevelTypes.values()) {
            String selector = "#TL" + tlt.name();
            
            cmd.append("#LLSidePanel #Content " + selector, "Pages/CosmeticGUI/TLButtons/" + tlt.name() + ".ui");
            evt.addEventBinding(CustomUIEventBindingType.Activating, "#LLSidePanel #Content " + selector + " #CategoryButton", EventData.of("TLT", tlt.name()));
            
            if (this.tlt != tlt) {
                cmd.set("#LLSidePanel #Content " + selector + " #CategoryButton.Visible", true);
                cmd.set("#LLSidePanel #Content " + selector + " #CategoryButtonEnabled.Visible", false);
                continue;
            }
            
            cmd.set("#LLSidePanel #Content " + selector + " #CategoryButton.Visible", false);
            cmd.set("#LLSidePanel #Content " + selector + " #CategoryButtonEnabled.Visible", true);
            
            cmd.append("#LSidePanel #Content #CategoryButton", "Pages/CosmeticGUI/Categories/" + tlt.name() + ".ui");
            
            switch (tlt) {
                case Head -> {
                    setupCategoryButton(cmd, evt, "Haircut", AttachmentsRegistry.CharacterSlot.Haircuts);
                    setupCategoryButton(cmd, evt, "Eyebrows", AttachmentsRegistry.CharacterSlot.Eyebrows);
                    setupCategoryButton(cmd, evt, "Eyes", AttachmentsRegistry.CharacterSlot.Eyes);
                    setupCategoryButton(cmd, evt, "FacialHair", AttachmentsRegistry.CharacterSlot.Beards);
                    setupCategoryButton(cmd, evt, "HeadAccessories", AttachmentsRegistry.CosmeticSlot.Head);
                    setupCategoryButton(cmd, evt, "FaceAccessories", AttachmentsRegistry.CosmeticSlot.Face_Accessories);
                    setupCategoryButton(cmd, evt, "EarAccessories", AttachmentsRegistry.CosmeticSlot.Ears_Accessories);
                }
                case General -> {
                    setupCategoryButton(cmd, evt, "Underwear", AttachmentsRegistry.CosmeticSlot.Underwears);
                    setupCategoryButton(cmd, evt, "Face", AttachmentsRegistry.CharacterSlot.Faces);
                    setupCategoryButton(cmd, evt, "Mouth", AttachmentsRegistry.CharacterSlot.Mouths);
                    setupCategoryButton(cmd, evt, "Ears", AttachmentsRegistry.CharacterSlot.Ears);
                }
                case Torso -> {
                    setupCategoryButton(cmd, evt, "Undertops", AttachmentsRegistry.CosmeticSlot.Undertops);
                    setupCategoryButton(cmd, evt, "Overtops", AttachmentsRegistry.CosmeticSlot.Overtops);
                    setupCategoryButton(cmd, evt, "Gloves", AttachmentsRegistry.CosmeticSlot.Gloves);
                }
                case Legs -> {
                    setupCategoryButton(cmd, evt, "Pants", AttachmentsRegistry.CosmeticSlot.Pants);
                    setupCategoryButton(cmd, evt, "Overpants", AttachmentsRegistry.CosmeticSlot.Overpants);
                    setupCategoryButton(cmd, evt, "Shoes", AttachmentsRegistry.CosmeticSlot.Shoes);
                }
                case Capes -> {
                    setupCategoryButton(cmd, evt, "Capes", AttachmentsRegistry.CosmeticSlot.Capes);
                }
            }
        }
    }
    
    private void setupCategoryButton(UICommandBuilder cmd, UIEventBuilder evt, String groupName, AttachmentsRegistry.Slot slot) {
        if (this.currentSlot == slot) {
            cmd.set("#LSidePanel #Content #CategoryButton #" + groupName + " #CategoryButton.Visible", false);
            cmd.set("#LSidePanel #Content #CategoryButton #" + groupName + " #CategoryButtonEnabled.Visible", true);
        } else {
            cmd.set("#LSidePanel #Content #CategoryButton #" + groupName + " #CategoryButton.Visible", true);
            cmd.set("#LSidePanel #Content #CategoryButton #" + groupName + " #CategoryButtonEnabled.Visible", false);
        }
        
        evt.addEventBinding(CustomUIEventBindingType.Activating, "#LSidePanel #Content #CategoryButton #" + groupName + " #CategoryButton", EventData.of("Slot", slot.name()));
    }
    
    private void buildCosmetics(Ref<EntityStore> ref, UICommandBuilder cmd, UIEventBuilder evt) {
        Map<String, AttachmentsRegistry.Attachment> registry = AttachmentsRegistry.get().getAttachmentsRegistry();
        
        int itemsPerRow = 8;
        
        cmd.appendInline("#Content #CosmeticGrid", "Group #CosmeticRow { LayoutMode: Top; }");
        
        AttachmentsRegistry.Attachment[] entries = registry.values()
            .stream()
            .filter(a -> a.data().slot() == currentSlot)
            .toList()
            .toArray(new AttachmentsRegistry.Attachment[0]);
        
        String[] keys = Arrays.stream(entries)
            .map(e -> AttachmentsRegistry.get().getKey(e))
            .filter(Objects::nonNull)
            .toArray(String[]::new);
        
        boolean showVanish = currentSlot.getType() != AttachmentsRegistry.SlotType.CHARACTER;
        int totalItems = entries.length + (showVanish ? 1 : 0);
        
        for (int i = 0; i < totalItems; i++) {
            int rowIndex = i / itemsPerRow;
            int colIndex = i % itemsPerRow;
            
            if (colIndex == 0) {
                cmd.append("#CosmeticRow", "Pages/CosmeticGUI/CosmeticRow.ui");
            }
            
            String rowSelector = "#CosmeticRow[" + rowIndex + "]";
            cmd.append(rowSelector + " #CosmeticSlot", "Pages/CosmeticGUI/CosmeticSlot.ui");
            
            String slotSelector = rowSelector + " #CosmeticSlot[" + colIndex + "]";
            
            if (showVanish && i == 0) {
                cmd.set(slotSelector + " #Icon.AssetPath", "UI/Custom/Common/Categories/VanishPart.png");
                
                if (AttachmentsRegistry.get().isEmptySlot(ref, currentSlot)) {
                    cmd.set(slotSelector + " #Button.Visible", false);
                    cmd.set(slotSelector + " #ButtonEnabled.Visible", true);
                }
                
                evt.addEventBinding(CustomUIEventBindingType.Activating, slotSelector + " #Button", EventData.of("CosmeticId", "No" + currentSlot.name()).append("Enabled", "false"));
                evt.addEventBinding(CustomUIEventBindingType.Activating, slotSelector + " #ButtonEnabled", EventData.of("CosmeticId", "No" + currentSlot.name()).append("Enabled", "true"));
                cmd.set(slotSelector + " #VariantIcon.Visible", false);
            } else {
                int entryIndex = showVanish ? i - 1 : i;
                AttachmentsRegistry.Attachment entry = entries[entryIndex];
                String cosmeticId = keys[entryIndex];
                
                String icon = entry.data().icon();
                
                String equippedVariant = AttachmentsRegistry.get().getEquippedVariant(ref, cosmeticId);
                if (equippedVariant != null) {
                    icon = entry.data().variants().get(equippedVariant).icon();
                }
                
                cmd.set(slotSelector + " #Icon.AssetPath", icon);
                
                if (AttachmentsRegistry.get().containsChange(ref, cosmeticId)) {
                    cmd.set(slotSelector + " #Button.Visible", false);
                    cmd.set(slotSelector + " #ButtonEnabled.Visible", true);
                }
                
                evt.addEventBinding(CustomUIEventBindingType.Activating, slotSelector + " #Button", EventData.of("CosmeticId", cosmeticId).append("Enabled", "false"));
                evt.addEventBinding(CustomUIEventBindingType.Activating, slotSelector + " #ButtonEnabled", EventData.of("CosmeticId", cosmeticId).append("Enabled", "true"));
                
                Map<String, AttachmentsRegistry.Variant> variants = entry.data().variants();
                
                boolean variantLogic = variants != null && !variants.isEmpty();
                
                cmd.set(slotSelector + " #VariantIcon.Visible", variantLogic);
                
                if (!variantLogic) continue;
                
                evt.addEventBinding(CustomUIEventBindingType.RightClicking, slotSelector + " #Button", EventData.of("VariantId", cosmeticId));
                evt.addEventBinding(CustomUIEventBindingType.RightClicking, slotSelector + " #ButtonEnabled", EventData.of("VariantId", cosmeticId));
            }
        }
        
        if (variants == null || variants.isEmpty()) {
            cmd.set("#RSidePanel.Visible", false);
            return;
        }
        
        cmd.set("#RSidePanel.Visible", true);
        
        cmd.append("#RSidePanel #Content #VariantList #VariantSlot", "Pages/CosmeticGUI/VariantSlot.ui");
        
        AttachmentsRegistry.Attachment original = AttachmentsRegistry.get().getAttachmentsRegistry().get(variantOriginalId);
        
        cmd.set("#RSidePanel #Content #VariantList #VariantSlot[0] #Icon.AssetPath", original.data().icon());
        
        if (AttachmentsRegistry.get().isEquipped(ref, this.variantOriginalId)) {
            cmd.set("#RSidePanel #Content #VariantList #VariantSlot[0] #Button.Visible", false);
            cmd.set("#RSidePanel #Content #VariantList #VariantSlot[0] #ButtonEnabled.Visible", true);
        } else {
            cmd.set("#RSidePanel #Content #VariantList #VariantSlot[0] #Button.Visible", true);
            cmd.set("#RSidePanel #Content #VariantList #VariantSlot[0] #ButtonEnabled.Visible", false);
        }
        
        evt.addEventBinding(CustomUIEventBindingType.Activating, "#RSidePanel #Content #VariantList #VariantSlot[0] #Button", EventData.of("CosmeticId", this.variantOriginalId).append("Enabled", "false"));
        evt.addEventBinding(CustomUIEventBindingType.Activating, "#RSidePanel #Content #VariantList #VariantSlot[0] #ButtonEnabled", EventData.of("CosmeticId", this.variantOriginalId).append("Enabled", "true"));
        
        List<Map.Entry<String, AttachmentsRegistry.Variant>> usableVariants = variants.entrySet().stream().toList();
        for (int i = 0; i < usableVariants.size(); i++) {
            Map.Entry<String, AttachmentsRegistry.Variant> entry = usableVariants.get(i);
            
            String selector = "#VariantSlot[" + (i + 1) + "]";
            
            cmd.append("#RSidePanel #Content #VariantList #VariantSlot", "Pages/CosmeticGUI/VariantSlot.ui");
            
            cmd.set("#RSidePanel #Content #VariantList " + selector + " #Icon.AssetPath", entry.getValue().icon());
            
            String variantId = this.variantOriginalId + "$" + entry.getKey();
            
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
    }
    
    @Override
    public void handleDataEvent(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl Store<EntityStore> store, @NonNullDecl Data data) {
        super.handleDataEvent(ref, store, data);
        
        if (data.variantId != null) {
            this.variants = new HashMap<>();
            AttachmentsRegistry.Attachment attachment = AttachmentsRegistry.get().getAttachmentsRegistry().get(data.variantId);
            this.variants = attachment.data().variants();
            this.variantOriginalId = data.variantId;
            
            this.sendUpdate();
            this.rebuild();
            
            return;
        }
        
        if (data.tlt != null) {
            this.variants = new HashMap<>();
            this.variantOriginalId = null;
            this.tlt = AttachmentsRegistry.TopLevelTypes.valueOf(data.tlt);
            
            switch (this.tlt) {
                case Head -> this.currentSlot = AttachmentsRegistry.CharacterSlot.Haircuts;
                case General -> this.currentSlot = AttachmentsRegistry.CosmeticSlot.Underwears;
                case Torso -> this.currentSlot = AttachmentsRegistry.CosmeticSlot.Undertops;
                case Legs -> this.currentSlot = AttachmentsRegistry.CosmeticSlot.Pants;
                case Capes -> this.currentSlot = AttachmentsRegistry.CosmeticSlot.Capes;
            }
            
            this.sendUpdate();
            this.rebuild();
            
            return;
        }
        
        if (data.slot != null) {
            this.variants = new HashMap<>();
            this.variantOriginalId = null;
            this.currentSlot = AttachmentsRegistry.Slot.valueOf(data.slot);
            this.sendUpdate();
            this.rebuild();
            
            return;
        }
        
        String baseId = data.cosmeticId;
        if (baseId != null && baseId.contains("$")) {
            baseId = baseId.split("\\$")[0];
        }
        
        if (!Objects.equals(baseId, this.variantOriginalId)) {
            this.variants = new HashMap<>();
            this.variantOriginalId = null;
        }
        
        if (data.enabled.equals("true")) {
            if (!data.cosmeticId.contains("$")) {
                this.variants = new HashMap<>();
                this.variantOriginalId = null;
            }

            AttachmentsRegistry.get().removeCosmetic(ref, data.cosmeticId);
            this.sendUpdate();
            this.rebuild();
            
            return;
        }
        
        AttachmentsRegistry.get().addCosmetic(ref, data.cosmeticId, true);
        
        this.sendUpdate();
        this.rebuild();
    }
    
    public static class Data {
        private String cosmeticId;
        private String enabled;
        
        private String slot;
        
        private String tlt;
        
        private String variantId;
        
        public static final BuilderCodec<Data> CODEC = BuilderCodec.builder(Data.class, Data::new)
            .append(new KeyedCodec<>("CosmeticId", BuilderCodec.STRING), (data, value) -> data.cosmeticId = value, (data) -> data.cosmeticId)
            .add()
            .append(new KeyedCodec<>("Enabled", BuilderCodec.STRING), (data, value) -> data.enabled = value, (data) -> data.enabled)
            .add()
            .append(new KeyedCodec<>("Slot", BuilderCodec.STRING), (data, value) -> data.slot = value, (data) -> data.slot)
            .add()
            .append(new KeyedCodec<>("TLT", BuilderCodec.STRING), (data, value) -> data.tlt = value, (data) -> data.tlt)
            .add()
            .append(new KeyedCodec<>("VariantId", BuilderCodec.STRING), (data, value) -> data.variantId = value, (data) -> data.variantId)
            .add()
            .build();
    }
}
