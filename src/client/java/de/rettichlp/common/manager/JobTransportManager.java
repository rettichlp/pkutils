package de.rettichlp.common.manager;

import de.rettichlp.common.listener.MessageListener;
import lombok.NoArgsConstructor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;

import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Optional.ofNullable;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.regex.Pattern.compile;

@NoArgsConstructor
public class JobTransportManager implements MessageListener {

    private static final Pattern TRANSPORT_DELIVER_PATTERN = compile("^\\[Transport] Du hast eine Kiste abgeliefert\\.$");
    private static final Pattern DRINK_TRANSPORT_DELIVER_PATTERN = compile("^\\[Bar] Du hast eine Flasche abgegeben!$");

    private ClientPlayNetworkHandler networkHandler;

    @Override
    public void onMessage(String message) {
        this.networkHandler = ofNullable(MinecraftClient.getInstance().player)
                .map(clientPlayerEntity -> clientPlayerEntity.networkHandler)
                .orElseThrow();

        Matcher transportDeliverMatcher = TRANSPORT_DELIVER_PATTERN.matcher(message);
        if (transportDeliverMatcher.find()) {
            delayedAction(() -> this.networkHandler.sendChatCommand("droptransport"), SECONDS.toMillis(10));
            return;
        }

        Matcher drinTransportDeliverMatcher = DRINK_TRANSPORT_DELIVER_PATTERN.matcher(message);
        if (drinTransportDeliverMatcher.find()) {
            delayedAction(() -> this.networkHandler.sendChatCommand("dropdrink"), 2500);
        }
    }

    private void delayedAction(Runnable runnable, long milliseconds) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runnable.run();
            }
        }, milliseconds);
    }
}
