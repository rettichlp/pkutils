package de.rettichlp.pkutils.common.listener;

import static de.rettichlp.pkutils.PKUtilsClient.networkHandler;
import static de.rettichlp.pkutils.command.ToggleDchatCommand.dChatEnabled;
import static de.rettichlp.pkutils.command.ToggleFchatCommand.fChatEnabled;

public class ClientSendMessageListener {

    public void register(String message) {

        if (fChatEnabled) {
            networkHandler.sendChatCommand("/f " + message);
            return;
        }

        if (dChatEnabled) {
            networkHandler.sendChatCommand("/d " + message);
        }
    }
}
