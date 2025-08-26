package de.rettichlp.pkutils.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.rettichlp.pkutils.common.manager.CommandBase;
import de.rettichlp.pkutils.common.registry.PKUtilsCommand;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.PlayerListEntry;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static de.rettichlp.pkutils.PKUtilsClient.networkHandler;
import static java.util.regex.Pattern.compile;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.minecraft.command.CommandSource.suggestMatching;

@PKUtilsCommand(label = "acall")
public class ACallCommand extends CommandBase implements de.rettichlp.pkutils.listener.IMessageReceiveListener {

    private static final Pattern NUMBER_PATTERN = compile("^[a-zA-Z0-9_]+ gehört die Nummer (?<number>\\d+)\\.$");

    private String lastRetrievedNumber;

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
                        .executes(context -> {
                            String playerName = getString(context, "player");
                            this.lastRetrievedNumber = "";
                            networkHandler.sendChatCommand("nummer " + playerName);
                            delayedAction(() -> {
                                if (!this.lastRetrievedNumber.isEmpty()) {
                                    networkHandler.sendChatCommand("call " + this.lastRetrievedNumber);
                                }
                            }, 1000);
                            return 1;
                        }));
    }

    @Override
    public boolean onMessageReceive(String message) {
        Matcher numberMatcher = NUMBER_PATTERN.matcher(message);
        if (numberMatcher.matches()) {
            this.lastRetrievedNumber = numberMatcher.group("number");
        }

        return true;
    }
}
