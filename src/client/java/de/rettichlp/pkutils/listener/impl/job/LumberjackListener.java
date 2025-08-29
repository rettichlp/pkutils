package de.rettichlp.pkutils.listener.impl.job;

import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import de.rettichlp.pkutils.common.registry.PKUtilsListener;
import de.rettichlp.pkutils.listener.ICommandSendListener;
import org.jetbrains.annotations.NotNull;

import static de.rettichlp.pkutils.PKUtilsClient.networkHandler;

@PKUtilsListener
public class LumberjackListener extends PKUtilsBase implements ICommandSendListener {

    @Override
    public boolean onCommandSend(@NotNull String command) {
        if (command.equals("sägewerk")) {
            delayedAction(() -> networkHandler.sendChatCommand("findtree"), 1000);
        }

        return true;
    }
}
