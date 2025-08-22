package de.rettichlp.pkutils.command;

import com.mojang.brigadier.CommandDispatcher;
import de.rettichlp.pkutils.common.manager.CommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import org.jetbrains.annotations.NotNull;

import static de.rettichlp.pkutils.PKUtilsClient.factionManager;
import static de.rettichlp.pkutils.common.manager.FactionManager.ToggledChat.D_CHAT;
import static de.rettichlp.pkutils.common.manager.FactionManager.ToggledChat.NONE;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ToggleDChatCommand extends CommandManager {

    public void register(@NotNull CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
                literal("dd")
                        .executes(context -> {
                            factionManager.setToggledChat(factionManager.getToggledChat() == D_CHAT ? NONE : D_CHAT);
                            return 1;
                        })
        );
    }
}
