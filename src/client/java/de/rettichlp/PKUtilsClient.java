package de.rettichlp;

import de.rettichlp.common.manager.JobFisherManager;
import de.rettichlp.common.manager.JobTransportManager;
import de.rettichlp.common.manager.WantedManager;
import de.rettichlp.common.storage.Storage;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class PKUtilsClient implements ClientModInitializer {

    public static Storage storage = new Storage();

    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.

        JobFisherManager jobFisherManager = new JobFisherManager();
        JobTransportManager jobTransportManager = new JobTransportManager();
        WantedManager wantedManager = new WantedManager();
        ClientPlayConnectionEvents.JOIN.register((handler, sender, minecraftClient) -> {
            assert minecraftClient.player != null; // cannot be null at this point
            player = minecraftClient.player;
            networkHandler = minecraftClient.player.networkHandler;

        });

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String rawMessage = message.getString();

            jobFisherManager.onMessage(rawMessage);
            jobTransportManager.onMessage(rawMessage);
            wantedManager.onMessage(rawMessage);
        });
    }
}
