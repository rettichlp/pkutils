package de.rettichlp.pkutils;

import de.rettichlp.pkutils.common.registry.Registry;
import de.rettichlp.pkutils.common.services.ActivityService;
import de.rettichlp.pkutils.common.services.FactionService;
import de.rettichlp.pkutils.common.services.SyncService;
import de.rettichlp.pkutils.common.storage.Storage;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;

public class PKUtilsClient implements ClientModInitializer {

    public static final Storage storage = new Storage();

    public static ClientPlayerEntity player;
    public static ClientPlayNetworkHandler networkHandler;

    public static ActivityService activityService;
    public static FactionService factionService;
    public static SyncService syncService;

    private final Registry registry = new Registry();

    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.

        activityService = new ActivityService();
        factionService = new FactionService();
        syncService = new SyncService();

        ClientPlayConnectionEvents.JOIN.register((handler, sender, minecraftClient) -> minecraftClient.execute(() -> {
            assert minecraftClient.player != null; // cannot be null at this point
            player = minecraftClient.player;
            networkHandler = minecraftClient.player.networkHandler;

            this.registry.registerListeners();
        }));

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            this.registry.registerCommands(dispatcher);
        });
    }
}
