package de.rettichlp;

import de.rettichlp.common.manager.JobFisherManager;
import de.rettichlp.common.manager.JobTransportManager;
import de.rettichlp.common.manager.WantedManager;
import de.rettichlp.common.storage.Storage;
import net.fabricmc.api.ClientModInitializer;

import static net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents.GAME;

public class PKUtilsClient implements ClientModInitializer {

    public static Storage storage = new Storage();

    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.

        JobFisherManager jobFisherManager = new JobFisherManager();
        JobTransportManager jobTransportManager = new JobTransportManager();
        WantedManager wantedManager = new WantedManager();

        GAME.register((message, overlay) -> {
            String rawMessage = message.getString();

            jobFisherManager.onMessage(rawMessage);
            jobTransportManager.onMessage(rawMessage);
            wantedManager.onMessage(rawMessage);
        });
    }
}
