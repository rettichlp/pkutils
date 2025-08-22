package de.rettichlp.common.command;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import org.jetbrains.annotations.NotNull;

import java.util.Timer;
import java.util.TimerTask;

import static de.rettichlp.PKUtilsClient.*;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ADropMoneyCommand {

    private int step = 0;

    public void register(@NotNull CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
                literal("sync")
                        .executes(context -> {
                            new Timer().scheduleAtFixedRate(new TimerTask() {
                                @Override
                                public void run() {
                                    switch (step++) {
                                        case 1 -> networkHandler.sendChatCommand("bank abbuchen 15000");
                                        case 2 -> networkHandler.sendChatCommand("dropmoney");
                                        case 3 -> {
                                            networkHandler.sendChatCommand("bank einzahlen 15000");
                                            step = 0;
                                            this.cancel();
                                        }
                                    }
                                }
                            }, 0, 1000);

                            return 1;
                        })
        );
    }
}
