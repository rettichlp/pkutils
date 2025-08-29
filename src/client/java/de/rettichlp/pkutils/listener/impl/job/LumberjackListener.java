package de.rettichlp.pkutils.listener.impl.job;

import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import de.rettichlp.pkutils.common.registry.PKUtilsListener;
import de.rettichlp.pkutils.listener.ICommandSendListener;
import de.rettichlp.pkutils.listener.IMessageReceiveListener;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.pkutils.PKUtilsClient.networkHandler;
import static java.util.regex.Pattern.compile;

@PKUtilsListener
public class LumberjackListener extends PKUtilsBase implements ICommandSendListener, IMessageReceiveListener {

    private static final Pattern LUMBERJACK_START_PATTERN = compile("^\\[Holzfäller] Fälle \\d+ Bäume und bringe sie zu den Sägen zur Weiterverarbeitung!$");

    @Override
    public boolean onCommandSend(@NotNull String command) {
        if (command.equals("sägewerk")) {
            delayedAction(() -> networkHandler.sendChatCommand("findtree"), 1000);
        }

        return true;
    }

    @Override
    public boolean onMessageReceive(String message) {
        Matcher lumberjackStartMatcher = LUMBERJACK_START_PATTERN.matcher(message);
        if (lumberjackStartMatcher.find()) {
            delayedAction(() -> networkHandler.sendChatCommand("findtree"), 1000);
        }

        return true;
    }
}
