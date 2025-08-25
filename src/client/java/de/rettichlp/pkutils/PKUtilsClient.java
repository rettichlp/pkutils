package de.rettichlp.pkutils;

import de.rettichlp.pkutils.common.listener.impl.faction.BlacklistListener;
import de.rettichlp.pkutils.common.listener.impl.faction.FactionChatListener;
import de.rettichlp.pkutils.common.listener.impl.job.FisherListener;
import de.rettichlp.pkutils.common.listener.impl.job.GarbageManListener;
import de.rettichlp.pkutils.common.listener.impl.job.TransportListener;
import de.rettichlp.pkutils.common.manager.SyncManager;
import de.rettichlp.pkutils.common.listener.impl.faction.WantedListener;
import de.rettichlp.pkutils.common.storage.Storage;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.StringJoiner;

import static de.rettichlp.pkutils.common.registry.Registry.registerCommands;
import static java.lang.Character.isUpperCase;
import static java.util.Objects.isNull;

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

    private BlockPos lastPlayerPos = null;

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
        }));

        ClientTickEvents.END_CLIENT_TICK.register((server) -> {
            // ignore ticks until the player is initialized
            if (player == null || networkHandler == null) {
                return;
            }

            BlockPos blockPos = player.getBlockPos();
            if (isNull(this.lastPlayerPos) || !this.lastPlayerPos.equals(blockPos)) {
                this.lastPlayerPos = blockPos;
                garbageManListener.onMove(blockPos);
            }
        });

        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            // ignore messages until the player is initialized
            if (player == null || networkHandler == null) {
                return true;
            }

            String rawMessage = message.getString();

            boolean showMessage1 = blacklistListener.onMessageReceive(rawMessage);
            boolean showMessage2 = fisherListener.onMessageReceive(rawMessage);
            boolean showMessage3 = garbageManListener.onMessageReceive(rawMessage);
            boolean showMessage4 = transportListener.onMessageReceive(rawMessage);
            boolean showMessage5 = syncManager.onMessageReceive(rawMessage);
            boolean showMessage6 = wantedListener.onMessageReceive(rawMessage);

            if (rawMessage.equals("Du hast dein Ziel erreicht!")) {
                fisherListener.onNaviSpotReached();
                transportListener.onNaviSpotReached();
            }

            return showMessage1 && showMessage2 && showMessage3 && showMessage4 && showMessage5 && showMessage6;
        });

        ClientSendMessageEvents.ALLOW_CHAT.register(message -> {
            boolean sendMessage1 = factionChatListener.onMessageSend(message);

            return sendMessage1;
        });

        ClientSendMessageEvents.ALLOW_COMMAND.register(command -> {
            String[] parts = command.split(" ", 2); // split the message into command label and arguments
            String commandLabel = parts[0]; // get the command label

            if (containsUppercase(commandLabel)) {
                String labelLowerCase = commandLabel.toLowerCase(); // get the lowercase command label

                StringJoiner stringJoiner = new StringJoiner(" ");
                stringJoiner.add(labelLowerCase);

                if (parts.length > 1) {
                    stringJoiner.add(parts[1]);
                }

                networkHandler.sendChatCommand(stringJoiner.toString());
                return false;
            }

            return true;
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            registerCommands(dispatcher);
        });
    }

    private boolean containsUppercase(@NotNull String input) {
        for (char c : input.toCharArray()) {
            if (isUpperCase(c)) {
                return true;
            }
        }

        return false;
    }
}
