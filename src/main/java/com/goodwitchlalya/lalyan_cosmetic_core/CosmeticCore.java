package com.goodwitchlalya.lalyan_cosmetic_core;

import com.goodwitchlalya.lalyan_cosmetic_core.Util.AttachmentsRegistry;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.console.ConsoleSender;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.Universe;

import javax.annotation.Nonnull;

/**
 * This class serves as the entrypoint for your plugin. Use the setup method to register into game registries or add
 * event listeners.
 */
public class CosmeticCore extends JavaPlugin {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private final Universe universe = Universe.get();
    
    
    
    public CosmeticCore(@Nonnull JavaPluginInit init) {
        super(init);
        LOGGER.atInfo().log("Hello from " + this.getName() + " version " + this.getManifest().getVersion().toString());
    }

    @Override
    protected void start() {
        CommandManager.get().handleCommand(ConsoleSender.INSTANCE, "auth login device");
        CommandManager.get().handleCommand(ConsoleSender.INSTANCE, "auth persistence Encrypted");
        
    }

    @Override
    protected void setup() {
        LOGGER.atInfo().log("Setting up plugin " + this.getName());
        this.getCommandRegistry().registerCommand(new ExampleCommand(this.getName(), this.getManifest().getVersion().toString()));
        
        getEventRegistry().registerGlobal(PlayerReadyEvent.class, (event) -> {
            
            
            
            /* Adding custom attachments */
            AttachmentsRegistry.register("Alien_Antenna", AttachmentsRegistry.CosmeticSlot.Head);
            
            AttachmentsRegistry.finalizeRegister(event);
            
        });
        

    }

    @Override
    protected void shutdown() {//Plugin shutting down!

    }
}