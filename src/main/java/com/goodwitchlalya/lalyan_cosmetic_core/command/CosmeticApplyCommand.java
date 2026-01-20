package com.goodwitchlalya.lalyan_cosmetic_core.command;

import com.goodwitchlalya.lalyan_cosmetic_core.util.AttachmentsRegistry;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.Map;

/**
 * Subcommand to manually apply a cosmetic to a player.
 * Requires OP permissions.
 */
public class CosmeticApplyCommand extends AbstractPlayerCommand {
    
    private final Universe universe = Universe.get();
    private RequiredArg<String> cosmeticName;
    private OptionalArg<String> override;
    
    /**
     * Constructor for the 'apply' subcommand.
     * Defines the command's arguments and permissions.
     */
    public CosmeticApplyCommand() {
        super("apply", "Manually applies a cosmetic");
        this.cosmeticName = this.withRequiredArg("cosmetic name", "The cosmetic Id", ArgTypes.STRING);
        this.override = this.withOptionalArg("override", "whether to override other cosmetics, or stack the new one on top of them", ArgTypes.STRING);
        this.setPermissionGroups("OP");
        
    }
    
    /**
     * Executes the command logic.
     * Applies the specified cosmetic to the player who executed the command.
     */
    @Override
    protected void execute(@NonNullDecl CommandContext commandContext, @NonNullDecl Store<EntityStore> store, @NonNullDecl Ref<EntityStore> ref, @NonNullDecl PlayerRef playerRef, @NonNullDecl World world) {
        // Determine if existing cosmetics in the same slot should be overridden. Defaults to true.
        boolean overrideBool = (override.get(commandContext) == null) || !override.get(commandContext).equals("no");
        
        // Execute the cosmetic application on the world's main thread.
        universe.getWorld(playerRef.getWorldUuid()).execute(() -> {
            AttachmentsRegistry.get().addCosmetic(ref, cosmeticName.get(commandContext), overrideBool);
        });
    }
    
}