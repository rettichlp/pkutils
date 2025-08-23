package de.rettichlp.pkutils.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import de.rettichlp.pkutils.common.listener.IMessageReceiveListener;
import de.rettichlp.pkutils.common.manager.CommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.argument.GameProfileArgumentType.GameProfileArgument;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.pkutils.PKUtilsClient.networkHandler;
import static java.util.regex.Pattern.compile;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static net.minecraft.command.argument.GameProfileArgumentType.gameProfile;

public class ACallCommand extends CommandManager implements IMessageReceiveListener {

    private static final Pattern NUMBER_PATTERN = compile("^[a-zA-Z0-9_]+ gehört die Nummer (?<number>\\d+)\\.$");

    private boolean isActiveNumberRetrieving = false;
    private String number;

    @Override
    public void register(@NotNull CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
                literal("acall")
                        .then(argument("Player", gameProfile())
                                .executes(context -> {
                                    GameProfileArgument gameProfileArgument = context.getArgument("Player", GameProfileArgument.class);
                                    Collection<GameProfile> gameProfiles = gameProfileArgument.getNames((ServerCommandSource) context.getSource());
                                    String playerName = gameProfiles.iterator().next().getName();

                                    // trigger display of the number
                                    this.isActiveNumberRetrieving = true;
                                    networkHandler.sendChatCommand("nummer " + playerName);

                                    // use number to start the call
                                    delayedAction(() -> networkHandler.sendChatCommand("call " + ACallCommand.this.number), 1000);

                                    return 1;
                                }))
        );
    }

    @Override
    public boolean onMessageReceive(String message) {
        Matcher numberMatcher = NUMBER_PATTERN.matcher(message);
        if (numberMatcher.matches()) {
            this.number = numberMatcher.group("number");
        }

        return true;
    }
}
