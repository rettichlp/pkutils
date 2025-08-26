package de.rettichlp.pkutils.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.Suggestion;
import de.rettichlp.pkutils.common.manager.CommandBase;
import de.rettichlp.pkutils.common.registry.PKUtilsCommand;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.command.CommandSource;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static de.rettichlp.pkutils.PKUtilsClient.networkHandler;
import static net.minecraft.command.CommandSource.suggestMatching;

@PKUtilsCommand(label = "wsu", aliases = "wp")
public class WSUCommand extends CommandBase {

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> execute(@NotNull LiteralArgumentBuilder<FabricClientCommandSource> node) {
        return node
                .then(ClientCommandManager.argument("player", word())
                        .suggests((context, builder) -> {
                            List<String> list = networkHandler.getPlayerList().stream()
                                    .map(PlayerListEntry::getProfile)
                                    .map(GameProfile::getName)
                                    .toList();
                            return suggestMatching(list, builder);
                        })
                        .then(ClientCommandManager.argument("value", greedyString())
                                .suggests((context, builder) -> {
                                    CommandDispatcher<CommandSource> commandDispatcher = networkHandler.getCommandDispatcher();
                                    // parse output of real asu command, because tab-completion is working for the reason argument
                                    ParseResults<CommandSource> asuParseResultForReason = commandDispatcher.parse("asu RettichLP ", networkHandler.getCommandSource());
                                    // get suggestions for reason argument
                                    return commandDispatcher.getCompletionSuggestions(asuParseResultForReason).thenCompose(suggestions -> {
                                        suggestions.getList().stream()
                                                .map(Suggestion::getText)
                                                .filter(s -> s.toLowerCase().startsWith(builder.getRemainingLowerCase()))
                                                .forEach(builder::suggest);
                                        return builder.buildFuture();
                                    });
                                })
                                .executes(context -> {
                                    String playerName = StringArgumentType.getString(context, "player");
                                    String value = StringArgumentType.getString(context, "value");
                                    networkHandler.sendChatCommand("asu " + playerName + " " + value);
                                    return 1;
                                })));
    }
}
