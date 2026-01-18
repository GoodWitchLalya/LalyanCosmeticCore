package com.goodwitchlalya.lalyan_cosmetic_core.command;

import com.goodwitchlalya.lalyan_cosmetic_core.util.AttachmentsRegistry;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class CosmeticListCommand extends AbstractPlayerCommand {
    
    private final Universe universe = Universe.get();
    
    public CosmeticListCommand() {
        super("list", "Lists all Cosmetics found and loaded");
        this.setPermissionGroup(GameMode.Adventure); // Allows the command to be used by anyone, not just OP
    }
    
    @Override
    protected void execute(@NonNullDecl CommandContext commandContext, @NonNullDecl Store<EntityStore> store, @NonNullDecl Ref<EntityStore> ref, @NonNullDecl PlayerRef playerRef, @NonNullDecl World world) {
        universe.getWorld(playerRef.getWorldUuid()).execute(() -> {
            commandContext.sendMessage(Message.raw("Cosmetics:"));
            AttachmentsRegistry.get().getAttachmentsList().forEach(attachment -> {
                commandContext.sendMessage(Message.raw(String.format("- %s", attachment)));
            });
        });
    }
    
}