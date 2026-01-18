package com.goodwitchlalya.lalyan_cosmetic_core;

import com.goodwitchlalya.lalyan_cosmetic_core.interaction.OpenCosmeticPageInteraction;
import com.goodwitchlalya.lalyan_cosmetic_core.util.AttachmentsRegistry;
import com.goodwitchlalya.lalyan_cosmetic_core.util.FileManager;
import com.goodwitchlalya.lalyan_cosmetic_core.command.CosmeticCommand;
import com.goodwitchlalya.lalyan_cosmetic_core.component.CosmeticData;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.console.ConsoleSender;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.Universe;

import javax.annotation.Nonnull;


public class CosmeticCore extends JavaPlugin {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    
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
        CosmeticData.INSTANCE = getEntityStoreRegistry().registerComponent(CosmeticData.class, "LCC_CosmeticData", CosmeticData.CODEC);
        this.getCommandRegistry().registerCommand(new CosmeticCommand());
        
        getCodecRegistry(Interaction.CODEC).register("LCC_OpenCosmetics", OpenCosmeticPageInteraction.class, OpenCosmeticPageInteraction.CODEC);
        
        getEventRegistry().registerGlobal(PlayerReadyEvent.class, event -> {
            AttachmentsRegistry.get().rebuildSkinWithCosmetics(event.getPlayerRef());
        });
        
        FileManager.wakeUp();
    }

    @Override
    protected void shutdown() {//Plugin shutting down!

    }
}