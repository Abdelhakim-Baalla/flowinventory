package com.flowinventory.mixin;

import com.flowinventory.network.SortInventoryPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractInventoryScreen<PlayerScreenHandler> {

    public InventoryScreenMixin(PlayerScreenHandler screenHandler, PlayerInventory playerInventory, Text text) {
        super(screenHandler, playerInventory, text);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void addSortButton(CallbackInfo ci) {
        // Place button near the top right of the inventory window
        int buttonX = this.x + 125;
        int buttonY = this.y + 61;
        
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Sort ▼"), button -> {
            ClientPlayNetworking.send(SortInventoryPacket.ID, PacketByteBufs.empty());
        }).dimensions(buttonX, buttonY, 45, 15).build());
    }
}
