package com.goodwitchlalya.lalyan_cosmetic_core;

import com.goodwitchlalya.lalyan_cosmetic_core.compat.wardrobe.WardrobeCompatLayer;
import com.goodwitchlalya.lalyan_cosmetic_core.compat.wardrobe.WardrobeLoader;
import com.goodwitchlalya.lalyan_cosmetic_core.interaction.OpenCosmeticPageInteraction;
import com.goodwitchlalya.lalyan_cosmetic_core.util.AttachmentsRegistry;
import com.goodwitchlalya.lalyan_cosmetic_core.util.FileManager;
import com.goodwitchlalya.lalyan_cosmetic_core.command.CosmeticCommand;
import com.goodwitchlalya.lalyan_cosmetic_core.component.CosmeticData;
import com.hypixel.hytale.codec.lookup.Priority;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.ComponentUpdate;
import com.hypixel.hytale.protocol.ComponentUpdateType;
import com.hypixel.hytale.protocol.EntityUpdate;
import com.hypixel.hytale.protocol.Equipment;
import com.hypixel.hytale.protocol.packets.entities.EntityUpdates;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.console.ConsoleSender;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.entity.LivingEntityInventoryChangeEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Optional;

/**
 * Main class for the Lalyan Cosmetic Core plugin.
 * This class handles the plugin's lifecycle, including initialization, setup, and shutdown.
 */
public class CosmeticCore extends JavaPlugin {
    
    // Logger for the plugin.
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    
    /**
     * Constructor for the plugin.
     * @param init The initialization context provided by the Hytale server.
     */
    public CosmeticCore(@Nonnull JavaPluginInit init) {
        super(init);
        LOGGER.atInfo().log("Hello from " + this.getName() + " version " + this.getManifest().getVersion().toString());
    }
    
    /**
     * Logs a message at the INFO level.
     * @param message The message to log.
     */
    public static void log(String message) {
        LOGGER.atInfo().log(message);
    }
    
    /**
     * Called when the plugin is starting.
     * This is used here to perform initial authentication commands.
     */
    @Override
    protected void start() {
        if (Objects.equals(System.getenv("DEV_MODE"), "True")) {
            CommandManager.get().handleCommand(ConsoleSender.INSTANCE, "auth login device");
            CommandManager.get().handleCommand(ConsoleSender.INSTANCE, "auth persistence Encrypted");
        }
    }

    /**
     * Called to set up the plugin's components.
     * This method registers commands, components, interactions, and event listeners.
     */
    @Override
    protected void setup() {
        // Register the custom component for storing cosmetic data on entities.
        CosmeticData.INSTANCE = getEntityStoreRegistry().registerComponent(CosmeticData.class, "LCC_CosmeticData", CosmeticData.CODEC);
        
        // Register the main command for the plugin.
        this.getCommandRegistry().registerCommand(new CosmeticCommand());
        
        // Register the custom interaction for opening the cosmetic GUI.
        getCodecRegistry(Interaction.CODEC).register("LCC_OpenCosmetics", OpenCosmeticPageInteraction.class, OpenCosmeticPageInteraction.CODEC);
        
        getCodecRegistry(WardrobeCompatLayer.Appearance.CODEC)
            .register(Priority.DEFAULT, "Model", WardrobeCompatLayer.ModelAppearance.class, WardrobeCompatLayer.ModelAppearance.CODEC)
            .register(Priority.NORMAL, "Variant", WardrobeCompatLayer.VariantAppearance.class, WardrobeCompatLayer.VariantAppearance.CODEC);
        
        getCodecRegistry(WardrobeCompatLayer.TextureConfig.CODEC)
            .register(Priority.DEFAULT, "Static", WardrobeCompatLayer.StaticTextureConfig.class, WardrobeCompatLayer.StaticTextureConfig.CODEC)
            .register(Priority.NORMAL, "Gradient", WardrobeCompatLayer.GradientTextureConfig.class, WardrobeCompatLayer.GradientTextureConfig.CODEC)
            .register(Priority.NORMAL, "Variant", WardrobeCompatLayer.VariantTextureConfig.class, WardrobeCompatLayer.VariantTextureConfig.CODEC);
        
        // Register an event listener for when a player is ready, to apply their saved cosmetics.
        getEventRegistry().registerGlobal(PlayerReadyEvent.class, event -> {
            AttachmentsRegistry.get().rebuildSkinWithCosmetics(event.getPlayerRef());
        });
        
        // Trigger the initial loading of all cosmetic assets.
        FileManager.wakeUp();
        WardrobeLoader.wakeUp();
    }

    /**
     * Called when the plugin is shutting down.
     * Can be used for cleanup tasks.
     */
    @Override
    protected void shutdown() {
        // This space is reserved for any cleanup needed when the plugin is disabled.
    }
}