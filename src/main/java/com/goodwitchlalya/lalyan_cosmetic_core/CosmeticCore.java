package com.goodwitchlalya.lalyan_cosmetic_core;

import com.goodwitchlalya.lalyan_cosmetic_core.compat.wardrobe.WardrobeCompatLayer;
import com.goodwitchlalya.lalyan_cosmetic_core.compat.wardrobe.WardrobeLoader;
import com.goodwitchlalya.lalyan_cosmetic_core.interaction.OpenCosmeticPageInteraction;
import com.goodwitchlalya.lalyan_cosmetic_core.util.AttachmentsRegistry;
import com.goodwitchlalya.lalyan_cosmetic_core.util.FileManager;
import com.goodwitchlalya.lalyan_cosmetic_core.command.CosmeticCommand;
import com.goodwitchlalya.lalyan_cosmetic_core.component.CosmeticData;
import com.goodwitchlalya.lalyan_cosmetic_core.util.LccConfig;
import com.hypixel.hytale.codec.lookup.Priority;
import com.goodwitchlalya.lalyan_cosmetic_core.util.LuckpermsCompatibility;
import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.console.ConsoleSender;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.plugin.PluginManager;

//Luckperms
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.events.StartWorldEvent;
import com.hypixel.hytale.server.core.util.Config;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Set;

/**
 * Main class for the Lalyan Cosmetic Core plugin.
 * This class handles the plugin's lifecycle, including initialization, setup, and shutdown.
 */
public class CosmeticCore extends JavaPlugin {
    
    // Logger for the plugin.
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    
    private static CosmeticCore instance;
    
    private final Config<LccConfig> config = this.withConfig("config.json", LccConfig.CODEC);
    
    public static boolean isLuckpermsLoaded;
    
    /**
     * Constructor for the plugin.
     * @param init The initialization context provided by the Hytale server.
     */
    public CosmeticCore(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
        LOGGER.atInfo().log("Hello from " + this.getName() + " version " + this.getManifest().getVersion().toString());
    }

    /**
     * Gets the instance of the plugin.
     * @return The current instance of CosmeticCore.
     */
    public static CosmeticCore getInstance() {
        return instance;
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
        return "lalyan_cosmetic_core.cosmetic." + cosmeticId + ".use";
    }
    
    public static Set<String> cosmeticIdToPermissionUse(String cosmeticId) {
        return Set.of(cosmeticIdToPermissionStringUse(cosmeticId));
    }
    
    public static String slotIdToPermissionStringUse(String slotId) {
        return "lalyan_cosmetic_core.slot." + slotId + ".use";
    }
    
    public static Set<String> slotIdToPermissionUse(String slotId) {
        return Set.of(slotIdToPermissionStringUse(slotId));
    }
    
    public static void addPerm(PlayerRef playerRef, Set<String> permission) {
        if (isLuckpermsLoaded) {
            try {
                LuckpermsCompatibility.getInstance().addPerm(playerRef, permission.stream().findFirst().get());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            PermissionsModule.get().addUserPermission(playerRef.getUuid(), permission);
        }
    }
    
    public static void addPerm(String group, Set<String> permission) {
        if (isLuckpermsLoaded) {
            try {
                LuckpermsCompatibility.getInstance().addPerm(group, permission.stream().findFirst().get());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            PermissionsModule.get().addGroupPermission(group, permission);
        }
    }
    
    public static void removePerm(PlayerRef playerRef, Set<String> permission) {
        if (isLuckpermsLoaded) {
            try {
                LuckpermsCompatibility.getInstance().removePerm(playerRef, permission.stream().findFirst().get());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            PermissionsModule.get().removeUserPermission(playerRef.getUuid(), permission);
        }
    }
    
    public static void removePerm(String group, Set<String> permission) {
        if (isLuckpermsLoaded) {
            try {
                LuckpermsCompatibility.getInstance().removePerm(group, permission.stream().findFirst().get());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            PermissionsModule.get().removeGroupPermission(group, permission);
        }
    }
    
    public static boolean getPerm(PlayerRef playerRef, Set<String> permission) {
        if (isLuckpermsLoaded) {
            try {
                return LuckpermsCompatibility.getInstance().getPerm(playerRef, permission.stream().findFirst().get());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            return PermissionsModule.get().hasPermission(playerRef.getUuid(), permission.stream().findFirst().get());
        }
    }
    
    public static boolean getSlotPerm(PlayerRef playerRef, AttachmentsRegistry.Slot slot) {
        CosmeticCore.log("Player ref: " + playerRef.getUsername());
        CosmeticCore.log("Slot ID: " + slot.name);
        CosmeticCore.log("Perm: " + CosmeticCore.slotIdToPermissionStringUse(slot.name));
        
        boolean permCheck = !CosmeticCore.getPerm(playerRef, CosmeticCore.slotIdToPermissionUse(slot.name));
        boolean configCheck = !CosmeticCore.instance.config.get().getPermissions();
        
        return permCheck || configCheck;
    }
    
    public static boolean getCosmeticPerm(PlayerRef playerRef, AttachmentsRegistry.Attachment attachment) {
        CosmeticCore.log("Player ref: " + playerRef.getUsername());
        CosmeticCore.log("Cosmetic ID: " + attachment.name());
        CosmeticCore.log("Perm: " + CosmeticCore.cosmeticIdToPermissionStringUse(attachment.name()));
        boolean cosmeticCheck = getPerm(playerRef, CosmeticCore.cosmeticIdToPermissionUse(attachment.name()));
        boolean slotCheck = getPerm(playerRef, CosmeticCore.slotIdToPermissionUse(attachment.data().slot.name));
        
        boolean permCheck = cosmeticCheck && slotCheck;
        boolean configCheck = !CosmeticCore.instance.config.get().getPermissions();
        
        return permCheck || configCheck;
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
            log("LuckPerms is loaded");
        } catch (Exception e) {
            isLuckpermsLoaded = false;
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
        
        config.save();// Ensures the config file is created if it doesn't exist
        
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
        
        getEventRegistry().registerGlobal(StartWorldEvent.class, event -> {
            // Trigger the initial loading of all cosmetic assets.
            FileManager.wakeUp();
            WardrobeLoader.wakeUp();
        });
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