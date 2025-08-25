package de.rettichlp.pkutils.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.rettichlp.pkutils.common.manager.CommandBase;
import de.rettichlp.pkutils.common.registry.PKUtilsCommand;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import org.jetbrains.annotations.NotNull;

import static de.rettichlp.pkutils.PKUtilsClient.factionManager;
import static de.rettichlp.pkutils.common.manager.FactionManager.ToggledChat.F_CHAT;
import static de.rettichlp.pkutils.common.manager.FactionManager.ToggledChat.NONE;

@PKUtilsCommand(label = "ff")
public class ToggleFChatCommand extends CommandBase {

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> execute(@NotNull LiteralArgumentBuilder<FabricClientCommandSource> node) {
        return node
                .executes(context -> {
                    factionManager.setToggledChat(factionManager.getToggledChat() == F_CHAT ? NONE : F_CHAT);
                    return 1;
                });
    }
}
