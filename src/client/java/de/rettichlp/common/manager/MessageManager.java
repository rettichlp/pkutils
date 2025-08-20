package de.rettichlp.common.manager;

import com.mojang.brigadier.Message;
import lombok.NoArgsConstructor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;

@NoArgsConstructor
public class MessageManager {

    private ClientPlayNetworkHandler networkHandler;
    private String rawMessage;

    public void process(Message message) {
        this.networkHandler = MinecraftClient.getInstance().player.networkHandler;
        this.rawMessage = message.getString();
    }
}
