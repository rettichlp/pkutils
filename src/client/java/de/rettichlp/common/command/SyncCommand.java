package de.rettichlp.common.command;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import org.jetbrains.annotations.NotNull;

import static de.rettichlp.PKUtilsClient.syncManager;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class SyncCommand {

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
