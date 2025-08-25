package de.rettichlp.pkutils.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.rettichlp.pkutils.common.manager.CommandBase;
import de.rettichlp.pkutils.common.registry.PKUtilsCommand;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import org.jetbrains.annotations.NotNull;

import static de.rettichlp.pkutils.PKUtilsClient.factionChatListener;
import static de.rettichlp.pkutils.common.listener.impl.faction.FactionChatListener.ToggledChat.D_CHAT;
import static de.rettichlp.pkutils.common.listener.impl.faction.FactionChatListener.ToggledChat.NONE;

@PKUtilsCommand(label = "dd")
public class ToggleDChatCommand extends CommandBase {

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> execute(@NotNull LiteralArgumentBuilder<FabricClientCommandSource> node) {
        return node
                .executes(context -> {
                    factionChatListener.setToggledChat(factionChatListener.getToggledChat() == D_CHAT ? NONE : D_CHAT);
                    return 1;
                });
    }
}
