package de.rettichlp.pkutils;

import de.rettichlp.pkutils.command.ADropMoneyCommand;
import de.rettichlp.pkutils.command.PKUtilsCommand;
import de.rettichlp.pkutils.command.RichTaxesCommand;
import de.rettichlp.pkutils.command.SyncCommand;
import de.rettichlp.pkutils.command.ToggleDChatCommand;
import de.rettichlp.pkutils.command.ToggleFChatCommand;
import de.rettichlp.pkutils.common.manager.FactionManager;
import de.rettichlp.pkutils.common.manager.JobFisherManager;
import de.rettichlp.pkutils.common.manager.JobGarbageManManager;
import de.rettichlp.pkutils.common.manager.JobTransportManager;
import de.rettichlp.pkutils.common.manager.SyncManager;
import de.rettichlp.pkutils.common.manager.WantedManager;
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

import static java.lang.Character.isUpperCase;
import static java.util.Objects.isNull;

public class PKUtilsClient implements ClientModInitializer {

    public static final Storage storage = new Storage();

    public static ClientPlayerEntity player;
    public static ClientPlayNetworkHandler networkHandler;

    // managers
    public static FactionManager factionManager;
    public static JobFisherManager jobFisherManager;
    public static JobTransportManager jobTransportManager;
    public static JobGarbageManManager jobGarbageManManager;
    public static SyncManager syncManager;
    public static WantedManager wantedManager;

    private BlockPos lastPlayerPos = null;

    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.

        factionManager = new FactionManager();
        jobFisherManager = new JobFisherManager();
        jobTransportManager = new JobTransportManager();
        jobGarbageManManager = new JobGarbageManManager();
        syncManager = new SyncManager();
        wantedManager = new WantedManager();

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
                jobGarbageManManager.onMove(blockPos);
            }
        });

        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            // ignore messages until the player is initialized
            if (player == null || networkHandler == null) {
                return true;
            }

            String rawMessage = message.getString();

            boolean showMessage1 = jobFisherManager.onMessageReceive(rawMessage);
            boolean showMessage2 = jobGarbageManManager.onMessageReceive(rawMessage);
            boolean showMessage3 = jobTransportManager.onMessageReceive(rawMessage);
            boolean showMessage4 = syncManager.onMessageReceive(rawMessage);
            boolean showMessage5 = wantedManager.onMessageReceive(rawMessage);

            if (rawMessage.equals("Du hast dein Ziel erreicht!")) {
                jobFisherManager.onNaviSpotReached();
                jobTransportManager.onNaviSpotReached();
            }

            return showMessage1 && showMessage2 && showMessage3 && showMessage4 && showMessage5;
        });

        ClientSendMessageEvents.ALLOW_CHAT.register(message -> {
            boolean sendMessage1 = factionManager.onMessageSend(message);

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

        ADropMoneyCommand aDropMoneyCommand = new ADropMoneyCommand();
        PKUtilsCommand pkUtilsCommand = new PKUtilsCommand();
        RichTaxesCommand richTaxesCommand = new RichTaxesCommand();
        SyncCommand syncCommand = new SyncCommand();
        ToggleDChatCommand toggleDChatCommand = new ToggleDChatCommand();
        ToggleFChatCommand toggleFChatCommand = new ToggleFChatCommand();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            aDropMoneyCommand.register(dispatcher);
            pkUtilsCommand.register(dispatcher);
            richTaxesCommand.register(dispatcher);
            syncCommand.register(dispatcher);
            toggleDChatCommand.register(dispatcher);
            toggleFChatCommand.register(dispatcher);
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
