package de.rettichlp.pkutils.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.rettichlp.pkutils.common.registry.CommandBase;
import de.rettichlp.pkutils.common.registry.PKUtilsCommand;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.PlayerListEntry;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static de.rettichlp.pkutils.PKUtilsClient.networkHandler;
import static de.rettichlp.pkutils.PKUtilsClient.storage;
import static de.rettichlp.pkutils.PKUtilsClient.syncService;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.minecraft.command.CommandSource.suggestMatching;

@PKUtilsCommand(label = "asms")
public class ASMSCommand extends CommandBase {

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> execute(@NotNull LiteralArgumentBuilder<FabricClientCommandSource> node) {
        return node
                .then(argument("player", word())
                        .suggests((context, builder) -> {
                            List<String> list = networkHandler.getPlayerList().stream()
                                    .map(PlayerListEntry::getProfile)
                                    .map(GameProfile::getName)
                                    .toList();
                            return suggestMatching(list, builder);
                        })
                        .then(argument("message", greedyString())
                                .executes(context -> {
                                    String playerName = getString(context, "player");
                                    String message = getString(context, "message");

                                    boolean numberAlreadyRetrieved = storage.getRetrievedNumbers().containsKey(playerName);
                                    if (numberAlreadyRetrieved) {
                                        int number = storage.getRetrievedNumbers().get(playerName);
                                        sms(number, message);
                                    } else {
                                        syncService.retrieveNumberAndRun(playerName, number -> sms(number, message));
                                    }

                                    return 1;
                                })));
    }

    private void sms(int number, String message) {
        networkHandler.sendChatCommand("sms " + number + " " + message);
    }
}
