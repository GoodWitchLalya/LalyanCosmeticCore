package com.goodwitchlalya.lalyan_cosmetic_core.gui.page;

import com.goodwitchlalya.lalyan_cosmetic_core.Util.AttachmentsRegistry;
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

import java.util.Map;

public class CosmeticPage extends InteractiveCustomUIPage<CosmeticPage.Data> {
    public CosmeticPage(@NonNullDecl PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss, Data.CODEC);
    }
    
    private AttachmentsRegistry.CosmeticSlot currentSlot = AttachmentsRegistry.CosmeticSlot.Capes;
    
    @Override
    public void build(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl UICommandBuilder cmd, @NonNullDecl UIEventBuilder evt, @NonNullDecl Store<EntityStore> store) {
        cmd.append("Pages/CosmeticGUI/LCC_CosmeticPage.ui");
        
        buildCosmeticButtons(evt);
        buildCosmetics(ref, cmd, evt);
    }
    
    private void buildCosmeticButtons(UIEventBuilder evt) {
        AttachmentsRegistry.CosmeticSlot[] values = AttachmentsRegistry.CosmeticSlot.values();
        
        for (int i = 0, valuesLength = values.length; i < valuesLength; i++) {
            AttachmentsRegistry.CosmeticSlot slot = values[i];
            String name = slot.name();
            
            if (name.contains("_")) {
                String[] split = name.split("_");
                name = split[0] + split[1];
            }
            
            String panel = i > 8? "#LeftSidePanel ": "#RightSidePanel ";
                
            evt.addEventBinding(CustomUIEventBindingType.Activating, panel + "#SidePanel #Content #SideButtons #" + name + " #CategoryButton", EventData.of("Slot", slot.name()));
        }
    }
    
    private void buildCosmetics(Ref<EntityStore> ref, UICommandBuilder cmd, UIEventBuilder evt) {
        Map<String, AttachmentsRegistry.Attachment> registry = AttachmentsRegistry.get().getAttachmentsRegistry();
        
        int amount = registry.size();
        int itemsPerRow = 8;
        
        cmd.appendInline("#Content #CosmeticGrid", "Group #CosmeticRow { LayoutMode: Top; }");
        
        int rowAmount = (int) Math.ceil((double) amount / itemsPerRow);
        int placedItems = 0;
        
        AttachmentsRegistry.Attachment[] entries = registry.values().toArray(new AttachmentsRegistry.Attachment[0]);
        String[] keys = registry.keySet().toArray(new String[0]);
        
        for (int x = 0; x < rowAmount; x++) {
            cmd.append("#CosmeticRow", "Pages/CosmeticGUI/CosmeticRow.ui");
            
            for (int y = 0; y < itemsPerRow; y++) {
                if(placedItems >= amount) break;
                
                int index  = (x * itemsPerRow) + y;
                
                placedItems++;
                cmd.append("#CosmeticRow[" + x + "] #CosmeticSlot", "Pages/CosmeticGUI/CosmeticSlot.ui");
                cmd.set("#CosmeticRow[" + x + "] #CosmeticSlot[" + y +"] #Icon.AssetPath", entries[index].icon());
                
                String cosmeticId = keys[index];
                
                if(AttachmentsRegistry.get().containsChange(ref, cosmeticId)) {
                    cmd.set("#CosmeticRow[" + x + "] #CosmeticSlot[" + y +"] #Button.Visible", false);
                    cmd.set("#CosmeticRow[" + x + "] #CosmeticSlot[" + y +"] #ButtonEnabled.Visible", true);
                }
                
                evt.addEventBinding(CustomUIEventBindingType.Activating, "#CosmeticRow[" + x + "] #CosmeticSlot[" + y +"] #Button", EventData.of("CosmeticId", cosmeticId).append("Enabled", "false"));
                evt.addEventBinding(CustomUIEventBindingType.Activating, "#CosmeticRow[" + x + "] #CosmeticSlot[" + y +"] #ButtonEnabled", EventData.of("CosmeticId", cosmeticId).append("Enabled", "true"));
            }
        }
    }
    
    @Override
    public void handleDataEvent(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl Store<EntityStore> store, @NonNullDecl Data data) {
        super.handleDataEvent(ref, store, data);
        
        if(data.slot != null) {
            this.currentSlot = AttachmentsRegistry.CosmeticSlot.valueOf(data.slot);
            this.sendUpdate();
            this.rebuild();
            
            return;
        }
        
        if(data.enabled.equals("true")) {
            AttachmentsRegistry.get().removeCosmetic(ref, data.cosmeticId);
            this.sendUpdate();
            this.rebuild();
            
            return;
        }
        
        AttachmentsRegistry.get().applyChange(ref, Map.of(data.cosmeticId, true));
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
