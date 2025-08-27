package de.rettichlp.pkutils.common.manager;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import org.jetbrains.annotations.NotNull;
import java.util.List;

public abstract class CommandBase extends PKUtilsBase {

    private List<PKUtilsBase> listeners;

    public abstract LiteralArgumentBuilder<FabricClientCommandSource> execute(@NotNull LiteralArgumentBuilder<FabricClientCommandSource> node);

    public void setListeners(List<PKUtilsBase> listeners) {
        this.listeners = listeners;
    }

    public List<PKUtilsBase> getListeners() {
        return this.listeners;
    }
}