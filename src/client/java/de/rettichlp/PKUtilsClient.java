package de.rettichlp;

import de.rettichlp.common.storage.Storage;
import net.fabricmc.api.ClientModInitializer;

public class PKUtilsClient implements ClientModInitializer {

    public static Storage storage = new Storage();

    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.
    }
}
