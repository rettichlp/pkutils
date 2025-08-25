package de.rettichlp.pkutils;

import de.rettichlp.pkutils.common.listener.impl.faction.BlacklistListener;
import de.rettichlp.pkutils.common.listener.impl.faction.FactionChatListener;
import de.rettichlp.pkutils.common.listener.impl.faction.WantedListener;
import de.rettichlp.pkutils.common.listener.impl.job.FisherListener;
import de.rettichlp.pkutils.common.listener.impl.job.GarbageManListener;
import de.rettichlp.pkutils.common.listener.impl.job.TransportListener;
import de.rettichlp.pkutils.common.manager.SyncManager;
import de.rettichlp.pkutils.common.registry.Registry;
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

    // managers
    public static BlacklistListener blacklistListener;
    public static FactionChatListener factionChatListener;
    public static FisherListener fisherListener;
    public static TransportListener transportListener;
    public static GarbageManListener garbageManListener;
    public static SyncManager syncManager;
    public static WantedListener wantedListener;

    private final Registry registry = new Registry();

    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.

        blacklistListener = new BlacklistListener();
        factionChatListener = new FactionChatListener();
        fisherListener = new FisherListener();
        transportListener = new TransportListener();
        garbageManListener = new GarbageManListener();
        syncManager = new SyncManager();
        wantedListener = new WantedListener();

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
