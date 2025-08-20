package de.rettichlp;

import de.rettichlp.common.manager.MessageManager;
import de.rettichlp.common.storage.Storage;
import net.fabricmc.api.ClientModInitializer;

import static net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents.GAME;

public class PKUtilsClient implements ClientModInitializer {

    public static Storage storage = new Storage();

    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.

        MessageManager messageManager = new MessageManager();

        GAME.register((message, overlay) -> messageManager.process(message));
    }
}
