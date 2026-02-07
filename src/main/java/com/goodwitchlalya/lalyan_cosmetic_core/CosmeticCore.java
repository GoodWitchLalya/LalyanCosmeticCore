package com.goodwitchlalya.lalyan_cosmetic_core;

import com.goodwitchlalya.lalyan_cosmetic_core.interaction.OpenCosmeticPageInteraction;
import com.goodwitchlalya.lalyan_cosmetic_core.util.AttachmentsRegistry;
import com.goodwitchlalya.lalyan_cosmetic_core.util.FileManager;
import com.goodwitchlalya.lalyan_cosmetic_core.command.CosmeticCommand;
import com.goodwitchlalya.lalyan_cosmetic_core.component.CosmeticData;
import com.goodwitchlalya.lalyan_cosmetic_core.util.LuckpermsCompatibility;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.console.ConsoleSender;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.plugin.PluginManager;

//Luckperms
import com.hypixel.hytale.server.core.universe.PlayerRef;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Main class for the Lalyan Cosmetic Core plugin.
 * This class handles the plugin's lifecycle, including initialization, setup, and shutdown.
 */
public class CosmeticCore extends JavaPlugin {
    
    // Gson instance for JSON serialization and deserialization.
    public static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .excludeFieldsWithoutExposeAnnotation()
        .create();
    
    // Logger for the plugin.
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    
    public static boolean isLuckpermsLoaded = false;
    
    public static Object luckpermsCompatibility;// Object -> LuckpermsCompatibility
    
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
     * Permissions formats:
     * Cosmetic: lalyancosmeticcore.cosmetic.COSMETIC_ID.use
     * Slot: lalyancosmeticcore.slot.SLOT_ID.use
     */
    public static String cosmeticIdToPermissionStringUse(String cosmeticId) {
        if (isLuckpermsLoaded) {
            return "goodwitchlalya.lalyan cosmetic core.cosmetic." + cosmeticId + ".use";
        } else {
            return "lalyancosmeticcore.cosmetic." + cosmeticId + ".use";
        }
    }
    public static Set<String> cosmeticIdToPermissionUse(String cosmeticId) {
        return Set.of(cosmeticIdToPermissionStringUse(cosmeticId));
    }
    public static String slotIdToPermissionStringUse(String slotId) {
        return "lalyancosmeticcore.slot." + slotId + ".use";
    }
    public static Set<String> slotIdToPermissionUse(String slotId) {
        return Set.of(slotIdToPermissionStringUse(slotId));
    }
    
    public static void addPerm(UUID uuid, String permission) {
        if (isLuckpermsLoaded) {
            //TODO
        } else {
            //TODO
        }
    }
    
    public static void addPerm(PlayerRef playerRef, String permission) {
        addPerm(playerRef.getUuid(), permission);
    }
    
    public static void addPerm(String group, String permission) {
        if (isLuckpermsLoaded) {
            //TODO
        } else {
            //TODO
        }
    }
    
    public static void removePerm(UUID uuid, String permission) {
        if (isLuckpermsLoaded) {
            //TODO
        } else {
            //TODO
        }
    }
    
    public static void removePerm(PlayerRef playerRef, String permission) {
        removePerm(playerRef.getUuid(), permission);
    }
    
    public static void removePerm(String group, String permission) {
        if (isLuckpermsLoaded) {
            //TODO
        } else {
            //TODO
        }
    }
    
    /**
     * Called when the plugin is starting.
     * This is used here to perform initial authentication commands.
     */
    @Override
    protected void start() {
        try {
            Objects.requireNonNull(PluginManager.get().getPlugin(PluginIdentifier.fromString("LuckPerms:LuckPerms"))).isEnabled();
            isLuckpermsLoaded = true;
        } catch (Exception e) {
            isLuckpermsLoaded = false;
        }
        
        if (isLuckpermsLoaded) {
            luckpermsCompatibility = LuckpermsCompatibility.getInstance();
        }
        
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
        
        // Register an event listener for when a player is ready, to apply their saved cosmetics.
        getEventRegistry().registerGlobal(PlayerReadyEvent.class, event -> {
            AttachmentsRegistry.get().rebuildSkinWithCosmetics(event.getPlayerRef());
        });
        
        // Trigger the initial loading of all cosmetic assets.
        FileManager.wakeUp();
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