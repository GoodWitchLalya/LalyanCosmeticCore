package com.goodwitchlalya.lalyan_cosmetic_core;

import com.goodwitchlalya.lalyan_cosmetic_core.Util.AttachmentsRegistry;
import com.goodwitchlalya.lalyan_cosmetic_core.component.CosmeticData;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.console.ConsoleSender;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.Universe;

import javax.annotation.Nonnull;

/**
 * This class serves as the entrypoint for your plugin. Use the setup method to register into game registries or add
 * event listeners.
 *
 * AssetPackRegisterEvent
 *
 */
public class CosmeticCore extends JavaPlugin {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private final Universe universe = Universe.get();
    
    public CosmeticCore(@Nonnull JavaPluginInit init) {
        super(init);
        LOGGER.atInfo().log("Hello from " + this.getName() + " version " + this.getManifest().getVersion().toString());
    }
    
    public static void log(String message) {
        LOGGER.atInfo().log(message);
    }
    
    @Override
    protected void start() {
        CommandManager.get().handleCommand(ConsoleSender.INSTANCE, "auth login device");
        CommandManager.get().handleCommand(ConsoleSender.INSTANCE, "auth persistence Encrypted");
        
    }

    @Override
    protected void setup() {
        CosmeticData.INSTANCE = getEntityStoreRegistry().registerComponent(CosmeticData.class, "CCL_CosmeticData", CosmeticData.CODEC);
        
        this.getCommandRegistry().registerCommand(new OpenCosmeticPage());
        this.getCommandRegistry().registerCommand(new CosmeticCommand(this.getName(), this.getManifest().getVersion().toString()));
        
        AttachmentsRegistry.get().register("Alien_Antenna", AttachmentsRegistry.CosmeticSlot.Head);
        AttachmentsRegistry.get().register("Beanie_Cactee", AttachmentsRegistry.CosmeticSlot.Head);
        AttachmentsRegistry.get().register("Bee_Antenna", AttachmentsRegistry.CosmeticSlot.Head);
        AttachmentsRegistry.get().register("Cloak_Chippy", AttachmentsRegistry.CosmeticSlot.Head);
        AttachmentsRegistry.get().register("Hat_Chippy", AttachmentsRegistry.CosmeticSlot.Head);
        AttachmentsRegistry.get().register("Headband_Littlewood", AttachmentsRegistry.CosmeticSlot.Head);
        AttachmentsRegistry.get().register("Headband_Paige", AttachmentsRegistry.CosmeticSlot.Head);
        AttachmentsRegistry.get().register("Kweebec_Mask", AttachmentsRegistry.CosmeticSlot.Head);
        AttachmentsRegistry.get().register("Kweebec_Mask_Straw", AttachmentsRegistry.CosmeticSlot.Head);
        AttachmentsRegistry.get().register("Scarak_Defender_Mask", AttachmentsRegistry.CosmeticSlot.Head);
        AttachmentsRegistry.get().register("Slothian_Mask", AttachmentsRegistry.CosmeticSlot.Head);
        
        AttachmentsRegistry.get().register("Jacket_Racing", AttachmentsRegistry.CosmeticSlot.Overtops);
        AttachmentsRegistry.get().register("Jacket_Violet", AttachmentsRegistry.CosmeticSlot.Overtops);
        AttachmentsRegistry.get().register("Overalls", AttachmentsRegistry.CosmeticSlot.Overtops);
        AttachmentsRegistry.get().register("Sweater", AttachmentsRegistry.CosmeticSlot.Overtops);
    }

    @Override
    protected void shutdown() {//Plugin shutting down!

    }
}