package de.rettichlp.pkutils;

import de.rettichlp.pkutils.command.*;
import de.rettichlp.pkutils.common.listener.ClientSendMessageListener;
import de.rettichlp.pkutils.common.manager.JobFisherManager;
import de.rettichlp.pkutils.common.manager.JobTransportManager;
import de.rettichlp.pkutils.common.manager.SyncManager;
import de.rettichlp.pkutils.common.storage.Storage;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.StringJoiner;

import static java.lang.Character.isUpperCase;

public class PKUtilsClient implements ClientModInitializer {

    public static ClientPlayerEntity player;
    public static ClientPlayNetworkHandler networkHandler;

    public static Storage storage = new Storage();

    // managers
    public static FactionManager factionManager;
    public static JobFisherManager jobFisherManager;
    public static JobTransportManager jobTransportManager;
    public static SyncManager syncManager;

    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.

        factionManager = new FactionManager();
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

            boolean showMessage1 = jobFisherManager.onMessageReceive(rawMessage);
            boolean showMessage2 = jobTransportManager.onMessageReceive(rawMessage);
            boolean showMessage3 = syncManager.onMessageReceive(rawMessage);

            return showMessage1 && showMessage2 && showMessage3;
        });

        ClientSendMessageEvents.ALLOW_CHAT.register(message -> {
            boolean sendMessage1 = factionManager.onMessageSend(message);

            return sendMessage1;
        });

        ClientSendMessageEvents.ALLOW_COMMAND.register(command -> {
            String[] parts = command.split(" ", 2); // split the message into command label and arguments
            String commandLabel = parts[0]; // get the command label

            if (containsUppercase(commandLabel)) {
                String labelLowerCase = commandLabel.toLowerCase(); // get the command label in lowercase

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
        RichTaxesCommand richTaxesCommand = new RichTaxesCommand();
        SyncCommand syncCommand = new SyncCommand();
        ToggleDchatCommand toggleDchatCommand = new ToggleDchatCommand();
        ToggleFchatCommand toggleFchatCommand = new ToggleFchatCommand();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            aDropMoneyCommand.register(dispatcher);
            richTaxesCommand.register(dispatcher);
            syncCommand.register(dispatcher);
            toggleDchatCommand.register(dispatcher);
            toggleFchatCommand.register(dispatcher);
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
