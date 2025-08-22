package de.rettichlp.pkutils.command;

import com.mojang.brigadier.CommandDispatcher;
import de.rettichlp.pkutils.common.manager.CommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import org.jetbrains.annotations.NotNull;

import static de.rettichlp.pkutils.PKUtilsClient.syncManager;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class SyncCommand extends CommandManager {

    @Override
    public void register(@NotNull CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
                literal("sync")
                        .executes(context -> {
                            syncManager.executeSync();
                            return 1;
                        })
        );
    }
}
