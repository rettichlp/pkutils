package de.rettichlp;

import de.rettichlp.common.manager.FactionManager;
import de.rettichlp.common.manager.JobFisherManager;
import de.rettichlp.common.manager.JobTransportManager;
import de.rettichlp.common.manager.WantedManager;
import de.rettichlp.common.storage.Storage;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;

public class PKUtilsClient implements ClientModInitializer {

    public static ClientPlayerEntity player;
    public static ClientPlayNetworkHandler networkHandler;

    public static Storage storage = new Storage();

    // managers
    public static FactionManager factionManager;
    public static JobFisherManager jobFisherManager;
    public static JobTransportManager jobTransportManager;
    public static WantedManager wantedManager;

    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.

        factionManager = new FactionManager();
        jobFisherManager = new JobFisherManager();
        jobTransportManager = new JobTransportManager();
        wantedManager = new WantedManager();

        ClientPlayConnectionEvents.JOIN.register((handler, sender, minecraftClient) -> minecraftClient.execute(() -> {
            assert minecraftClient.player != null; // cannot be null at this point
            player = minecraftClient.player;
            networkHandler = minecraftClient.player.networkHandler;

            factionManager.onJoin();
            wantedManager.onJoin();
        }));

        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            // ignore messages until the player is initialized
            if (player == null || networkHandler == null) {
                return true;
            }

            String rawMessage = message.getString();

            boolean showMessage1 = factionManager.onMessage(rawMessage);
            boolean showMessage2 = jobFisherManager.onMessage(rawMessage);
            boolean showMessage3 = jobTransportManager.onMessage(rawMessage);
            boolean showMessage4 = wantedManager.onMessage(rawMessage);

            storage.print();

            return showMessage1 && showMessage2 && showMessage3 && showMessage4;
        });
    }
}
