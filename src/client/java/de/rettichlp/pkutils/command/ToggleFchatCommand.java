package de.rettichlp.pkutils.command;

import com.mojang.brigadier.CommandDispatcher;
import de.rettichlp.pkutils.common.manager.CommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.Timer;
import java.util.TimerTask;

import static de.rettichlp.pkutils.PKUtilsClient.networkHandler;
import static de.rettichlp.pkutils.PKUtilsClient.player;
import static de.rettichlp.pkutils.command.ToggleDchatCommand.dChatEnabled;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ToggleFchatCommand extends CommandManager {

    public static boolean fChatEnabled = false;

    public void register(@NotNull CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
                literal("ff")
                        .executes(context -> {

                            if (dChatEnabled) {
                                dChatEnabled = false;
                            }

                            fChatEnabled = !fChatEnabled;
                            sendModMessage("Du hast den Fraktionschat " + (fChatEnabled ? "aktiviert" : "deaktiviert") + ".", false);
                            return 1;
                        })
        );
    }
}
