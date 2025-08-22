package de.rettichlp.pkutils.common.manager;

import de.rettichlp.pkutils.common.listener.IMessageSendListener;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static de.rettichlp.pkutils.PKUtilsClient.networkHandler;
import static de.rettichlp.pkutils.common.manager.FactionManager.ToggledChat.NONE;

@NoArgsConstructor
public class FactionManager extends BaseManager implements IMessageSendListener {

    @Getter
    private ToggledChat toggledChat = NONE;

    public void setToggledChat(ToggledChat toggledChat) {
        this.toggledChat = toggledChat;
        sendModMessage(this.toggledChat.getToggleMessage(), false);
    }

    @Override
    public boolean onMessageSend(String message) {
        if (this.toggledChat != NONE) {
            networkHandler.sendChatCommand(this.toggledChat.getCommand() + " " + message);
            return false;
        }

        return true;
    }

    @Getter
    @AllArgsConstructor
    public enum ToggledChat {

        NONE("", "Dauerhafter Fraktionschat deaktiviert."),
        D_CHAT("d", "Dauerhafter D-Chat aktiviert."),
        F_CHAT("f", "Dauerhafter F-Chat aktiviert.");

        private final String command;
        private final String toggleMessage;
    }
}
