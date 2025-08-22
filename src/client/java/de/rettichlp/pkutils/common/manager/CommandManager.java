package de.rettichlp.pkutils.common.manager;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import org.jetbrains.annotations.NotNull;

public abstract class CommandManager extends BaseManager {

    public abstract void register(@NotNull CommandDispatcher<FabricClientCommandSource> dispatcher);
}
