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
 * This is an example command that will simply print the name of the plugin in chat when used.
 */
public class CosmeticApplyCommand extends AbstractPlayerCommand {
    
    private final Universe universe = Universe.get();
    private RequiredArg<String> cosmeticName;
    private OptionalArg<String> override;
    private boolean overrideBool;
    
    public CosmeticApplyCommand() {
        super("apply", "");
        this.cosmeticName = this.withRequiredArg("cosmetic name", "", ArgTypes.STRING);
        this.override = this.withOptionalArg("override", "", ArgTypes.STRING);
        this.setPermissionGroup(GameMode.Adventure); // Allows the command to be used by anyone, not just OP
        
    }
    
    @Override
    protected void execute(@NonNullDecl CommandContext commandContext, @NonNullDecl Store<EntityStore> store, @NonNullDecl Ref<EntityStore> ref, @NonNullDecl PlayerRef playerRef, @NonNullDecl World world) {
        
        if ((override.get(commandContext) != null) && ((override.get(commandContext).equals("no")))) {
            this.overrideBool = false;
        } else {
            this.overrideBool = true;
        }
        
        universe.getWorld(playerRef.getWorldUuid()).execute(() -> {
            
            AttachmentsRegistry.get().applyChanges(ref, Map.of(cosmeticName.get(commandContext), overrideBool));
            
        });
    }
    
}