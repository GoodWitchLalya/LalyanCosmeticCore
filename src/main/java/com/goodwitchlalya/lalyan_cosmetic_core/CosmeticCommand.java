package com.goodwitchlalya.lalyan_cosmetic_core;

import com.goodwitchlalya.lalyan_cosmetic_core.Util.AttachmentsRegistry;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.UUID;

/**
 * This is an example command that will simply print the name of the plugin in chat when used.
 */
public class CosmeticCommand extends CommandBase {

    private final String pluginName;
    private final String pluginVersion;
    private final Universe universe = Universe.get();

    public CosmeticCommand(String pluginName, String pluginVersion) {
        super("cosmetic", "Prints a test message from the " + pluginName + " plugin.");
        this.setPermissionGroup(GameMode.Adventure); // Allows the command to be used by anyone, not just OP
        this.pluginName = pluginName;
        this.pluginVersion = pluginVersion;
    }

    @Override
    protected void executeSync(@Nonnull CommandContext ctx) {
        UUID senderUUID = ctx.sender().getUuid();
        PlayerRef playerRef = universe.getPlayer(senderUUID);
        universe.getWorld(playerRef.getWorldUuid()).execute(() -> {
                    Ref<EntityStore> ref = playerRef.getReference();
                    AttachmentsRegistry.applyChange(ref, Map.of("Alien_Antenna", true));
        }
        );
        
    }
}