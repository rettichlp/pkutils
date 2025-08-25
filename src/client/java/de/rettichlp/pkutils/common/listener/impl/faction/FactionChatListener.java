package de.rettichlp.pkutils.common.listener.impl.faction;

import de.rettichlp.pkutils.common.listener.IMessageSendListener;
import de.rettichlp.pkutils.common.manager.PKUtilsBase;
import de.rettichlp.pkutils.common.registry.PKUtilsListener;
import de.rettichlp.pkutils.common.storage.Storage;

import static de.rettichlp.pkutils.PKUtilsClient.networkHandler;
import static de.rettichlp.pkutils.PKUtilsClient.storage;
import static de.rettichlp.pkutils.common.storage.Storage.ToggledChat.NONE;

@PKUtilsListener
public class FactionChatListener extends PKUtilsBase implements IMessageSendListener {

    @Override
    public boolean onMessageSend(String message) {
        Storage.ToggledChat toggledChat = storage.getToggledChat();
        if (toggledChat != NONE) {
            networkHandler.sendChatCommand(toggledChat.getCommand() + " " + message);
            return false;
        }

        return true;
    }
}
