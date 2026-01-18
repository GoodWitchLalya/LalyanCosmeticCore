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

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public class CosmeticPage extends InteractiveCustomUIPage<CosmeticPage.Data> {
    public CosmeticPage(@NonNullDecl PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss, Data.CODEC);
    }
    
    private AttachmentsRegistry.CosmeticSlot currentSlot = AttachmentsRegistry.CosmeticSlot.Capes;
    
    @Override
    public void build(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl UICommandBuilder cmd, @NonNullDecl UIEventBuilder evt, @NonNullDecl Store<EntityStore> store) {
        cmd.append("Pages/CosmeticGUI/LCC_CosmeticPage.ui");
        
        buildCosmeticButtons(cmd, evt);
        buildCosmetics(ref, cmd, evt);
    }
    
    private void buildCosmeticButtons(UICommandBuilder cmd, UIEventBuilder evt) {
        AttachmentsRegistry.CosmeticSlot[] values = AttachmentsRegistry.CosmeticSlot.values();
        
        for (int i = 0, valuesLength = values.length; i < valuesLength; i++) {
            AttachmentsRegistry.CosmeticSlot slot = values[i];
            String name = slot.name();
            
            if (name.contains("_")) {
                String[] split = name.split("_");
                name = split[0] + split[1];
            }
            
            evt.addEventBinding(CustomUIEventBindingType.Activating, "#LeftSidePanel #Content #SideButtons #" + name + " #CategoryButton", EventData.of("Slot", slot.name()));
            
            if (slot != currentSlot) {
                cmd.set("#LeftSidePanel #Content #SideButtons #" + name + " #CategoryButtonEnabled.Visible", false);
                cmd.set("#LeftSidePanel #Content #SideButtons #" + name + " #CategoryButton.Visible", true);
                
                continue;
            }
            
            cmd.set("#LeftSidePanel #Content #SideButtons #" + name + " #CategoryButtonEnabled.Visible", true);
            cmd.set("#LeftSidePanel #Content #SideButtons #" + name + " #CategoryButton.Visible", false);
        }
    }
    
    private void buildCosmetics(Ref<EntityStore> ref, UICommandBuilder cmd, UIEventBuilder evt) {
        Map<String, AttachmentsRegistry.Attachment> registry = AttachmentsRegistry.get().getAttachmentsRegistry();
        
        int itemsPerRow = 8;
        
        cmd.appendInline("#Content #CosmeticGrid", "Group #CosmeticRow { LayoutMode: Top; }");
        
        AttachmentsRegistry.Attachment[] entries = registry.values()
            .stream()
            .filter(a -> a.slot() == currentSlot)
            .toList()
            .toArray(new AttachmentsRegistry.Attachment[0]);
        
        String[] keys = Arrays.stream(entries)
            .map(e -> AttachmentsRegistry.get().getKey(e))
            .filter(Objects::nonNull)
            .toArray(String[]::new);
        
        int totalItems = entries.length + 1;
        
        for (int i = 0; i < totalItems; i++) {
            int rowIndex = i / itemsPerRow;
            int colIndex = i % itemsPerRow;
            
            if (colIndex == 0) {
                cmd.append("#CosmeticRow", "Pages/CosmeticGUI/CosmeticRow.ui");
            }
            
            String rowSelector = "#CosmeticRow[" + rowIndex + "]";
            cmd.append(rowSelector + " #CosmeticSlot", "Pages/CosmeticGUI/CosmeticSlot.ui");
            
            String slotSelector = rowSelector + " #CosmeticSlot[" + colIndex + "]";
            
            if (i == 0) {
                cmd.set(slotSelector + " #Icon.AssetPath", "UI/Custom/Common/Categories/VanishPart.png");
                
                if (AttachmentsRegistry.get().isEmptySlot(ref, currentSlot)) {
                    cmd.set(slotSelector + " #Button.Visible", false);
                    cmd.set(slotSelector + " #ButtonEnabled.Visible", true);
                }
                
                evt.addEventBinding(CustomUIEventBindingType.Activating, slotSelector + " #Button", EventData.of("CosmeticId", "No" + currentSlot.name()).append("Enabled", "false"));
                evt.addEventBinding(CustomUIEventBindingType.Activating, slotSelector + " #ButtonEnabled", EventData.of("CosmeticId", "No" + currentSlot.name()).append("Enabled", "true"));
            } else {
                int entryIndex = i - 1;
                AttachmentsRegistry.Attachment entry = entries[entryIndex];
                String cosmeticId = keys[entryIndex];
                
                cmd.set(slotSelector + " #Icon.AssetPath", entry.icon());
                
                if (AttachmentsRegistry.get().containsChange(ref, cosmeticId)) {
                    cmd.set(slotSelector + " #Button.Visible", false);
                    cmd.set(slotSelector + " #ButtonEnabled.Visible", true);
                }
                
                evt.addEventBinding(CustomUIEventBindingType.Activating, slotSelector + " #Button", EventData.of("CosmeticId", cosmeticId).append("Enabled", "false"));
                evt.addEventBinding(CustomUIEventBindingType.Activating, slotSelector + " #ButtonEnabled", EventData.of("CosmeticId", cosmeticId).append("Enabled", "true"));
            }
        }
    }
    
    @Override
    public void handleDataEvent(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl Store<EntityStore> store, @NonNullDecl Data data) {
        super.handleDataEvent(ref, store, data);
        
        if (data.slot != null) {
            this.currentSlot = AttachmentsRegistry.CosmeticSlot.valueOf(data.slot);
            this.sendUpdate();
            this.rebuild();
            
            return;
        }
        
        if (data.enabled.equals("true")) {
            AttachmentsRegistry.get().removeCosmetic(ref, data.cosmeticId);
            this.sendUpdate();
            this.rebuild();
            
            return;
        }
        
        AttachmentsRegistry.get().clearSlot(ref, currentSlot);
        AttachmentsRegistry.get().addCosmetic(ref, data.cosmeticId);
        
        this.sendUpdate();
        this.rebuild();
    }
    
    public static class Data {
        private String cosmeticId;
        private String enabled;
        
        private String slot;
        
        public static final BuilderCodec<Data> CODEC = BuilderCodec.builder(Data.class, Data::new)
            .append(new KeyedCodec<>("CosmeticId", BuilderCodec.STRING), (data, value) -> data.cosmeticId = value, (data) -> data.cosmeticId)
            .add()
            .append(new KeyedCodec<>("Enabled", BuilderCodec.STRING), (data, value) -> data.enabled = value, (data) -> data.enabled)
            .add()
            .append(new KeyedCodec<>("Slot", BuilderCodec.STRING), (data, value) -> data.slot = value, (data) -> data.slot)
            .add()
            .build();
    }
}
