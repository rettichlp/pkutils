package de.rettichlp.pkutils.command;

import com.mojang.brigadier.CommandDispatcher;
import de.rettichlp.pkutils.common.manager.CommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import org.jetbrains.annotations.NotNull;

import static de.rettichlp.pkutils.command.ToggleFchatCommand.fChatEnabled;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ToggleDchatCommand extends CommandManager {

    public static boolean dChatEnabled = false;

    public void register(@NotNull CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
                literal("ff")
                        .executes(context -> {

                            if (fChatEnabled) {
                                fChatEnabled = false;
                            }

                            dChatEnabled = !dChatEnabled;
                            sendModMessage("Du hast den Bündnischat  " + (dChatEnabled ? "aktiviert" : "deaktiviert") + ".", false);
                            return 1;
                        })
        );
    }
}
