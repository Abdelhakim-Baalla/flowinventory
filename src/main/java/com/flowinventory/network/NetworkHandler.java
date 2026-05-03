package com.flowinventory.network;

import com.flowinventory.FlowInventoryMod;
import com.flowinventory.profiles.ActivityType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.client.MinecraftClient;

/**
 * Centralized network communication handler for client-server packets.
 */
@Environment(EnvType.CLIENT)
public class NetworkHandler {
    
    public static void sendActivityChange(ActivityType activity) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getNetworkHandler() == null) return;
        
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(activity.name());
        ClientPlayNetworking.send(ActivityChangePacket.ID, buf);
    }
    
    public static void sendSortRequest() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getNetworkHandler() == null) return;

        ActivityType ctx = ActivityType.GENERAL;
        if (FlowInventoryMod.activityDetector != null) {
            ctx = FlowInventoryMod.activityDetector.getCurrentActivity();
        }
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(ctx.name());
        ClientPlayNetworking.send(SortInventoryPacket.ID, buf);
    }
}
