package de.rettichlp.pkutils;

import de.rettichlp.pkutils.command.ADropMoneyCommand;
import de.rettichlp.pkutils.command.RichTaxesCommand;
import de.rettichlp.pkutils.command.SyncCommand;
import de.rettichlp.pkutils.common.manager.JobFisherManager;
import de.rettichlp.pkutils.common.manager.JobTransportManager;
import de.rettichlp.pkutils.common.manager.SyncManager;
import de.rettichlp.pkutils.common.storage.Storage;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;

public class PKUtilsClient implements ClientModInitializer {

    public static ClientPlayerEntity player;
    public static ClientPlayNetworkHandler networkHandler;

    public static Storage storage = new Storage();

    // managers
    public static JobFisherManager jobFisherManager;
    public static JobTransportManager jobTransportManager;
    public static SyncManager syncManager;

    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.

        jobFisherManager = new JobFisherManager();
        jobTransportManager = new JobTransportManager();
        syncManager = new SyncManager();

        ClientPlayConnectionEvents.JOIN.register((handler, sender, minecraftClient) -> minecraftClient.execute(() -> {
            assert minecraftClient.player != null; // cannot be null at this point
            player = minecraftClient.player;
            networkHandler = minecraftClient.player.networkHandler;
        }));

        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            // ignore messages until the player is initialized
            if (player == null || networkHandler == null) {
                return true;
            }

            String rawMessage = message.getString();

            boolean showMessage1 = jobFisherManager.onMessage(rawMessage);
            boolean showMessage2 = jobTransportManager.onMessage(rawMessage);
            boolean showMessage3 = syncManager.onMessage(rawMessage);

            return showMessage1 && showMessage2 && showMessage3;
        });

        ADropMoneyCommand aDropMoneyCommand = new ADropMoneyCommand();
        RichTaxesCommand richTaxesCommand = new RichTaxesCommand();
        SyncCommand syncCommand = new SyncCommand();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            aDropMoneyCommand.register(dispatcher);
            richTaxesCommand.register(dispatcher);
            syncCommand.register(dispatcher);
        });
    }
}
