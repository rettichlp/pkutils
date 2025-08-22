package de.rettichlp.pkutils.common.manager;

import de.rettichlp.pkutils.common.listener.IMessageListener;
import lombok.NoArgsConstructor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.pkutils.PKUtilsClient.networkHandler;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.regex.Pattern.compile;

@NoArgsConstructor
public class JobTransportManager extends BaseManager implements IMessageListener {

    private static final Pattern TRANSPORT_DELIVER_PATTERN = compile("^\\[Transport] Du hast eine Kiste abgeliefert\\.$");
    private static final Pattern DRINK_TRANSPORT_DELIVER_PATTERN = compile("^\\[Bar] Du hast eine Flasche abgegeben!$");

    @Override
    public boolean onMessage(String message) {
        Matcher transportDeliverMatcher = TRANSPORT_DELIVER_PATTERN.matcher(message);
        if (transportDeliverMatcher.find()) {
            delayedAction(() -> networkHandler.sendChatCommand("droptransport"), SECONDS.toMillis(10));
            return true;
        }

        Matcher drinTransportDeliverMatcher = DRINK_TRANSPORT_DELIVER_PATTERN.matcher(message);
        if (drinTransportDeliverMatcher.find()) {
            delayedAction(() -> networkHandler.sendChatCommand("dropdrink"), 2500);
            return true;
        }

        return true;
    }
}
