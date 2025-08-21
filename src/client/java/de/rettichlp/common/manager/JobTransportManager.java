package de.rettichlp.common.manager;

import de.rettichlp.common.listener.MessageListener;
import lombok.NoArgsConstructor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.PKUtilsClient.networkHandler;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.regex.Pattern.compile;

@NoArgsConstructor
public class JobTransportManager extends BaseManager implements MessageListener {

    private static final Pattern TRANSPORT_DELIVER_PATTERN = compile("^\\[Transport] Du hast eine Kiste abgeliefert\\.$");
    private static final Pattern DRINK_TRANSPORT_DELIVER_PATTERN = compile("^\\[Bar] Du hast eine Flasche abgegeben!$");

    @Override
    public void onMessage(String message) {
        Matcher transportDeliverMatcher = TRANSPORT_DELIVER_PATTERN.matcher(message);
        if (transportDeliverMatcher.find()) {
            delayedAction(() -> networkHandler.sendChatCommand("droptransport"), SECONDS.toMillis(10));
            return;
        }

        Matcher drinTransportDeliverMatcher = DRINK_TRANSPORT_DELIVER_PATTERN.matcher(message);
        if (drinTransportDeliverMatcher.find()) {
            delayedAction(() -> networkHandler.sendChatCommand("dropdrink"), 2500);
        }
    }
}
