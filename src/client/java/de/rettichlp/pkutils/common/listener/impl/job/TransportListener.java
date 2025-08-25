package de.rettichlp.pkutils.common.listener.impl.job;

import de.rettichlp.pkutils.common.listener.IMessageReceiveListener;
import de.rettichlp.pkutils.common.listener.INaviSpotReachedListener;
import de.rettichlp.pkutils.common.manager.ManagerBase;
import lombok.NoArgsConstructor;
import net.minecraft.util.math.BlockPos;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.pkutils.PKUtilsClient.networkHandler;
import static de.rettichlp.pkutils.PKUtilsClient.player;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.regex.Pattern.compile;

@NoArgsConstructor
public class TransportListener extends ManagerBase implements IMessageReceiveListener, INaviSpotReachedListener {

    private static final Pattern TRANSPORT_DELIVER_PATTERN = compile("^\\[Transport] Du hast eine (Kiste|Waffenkiste) abgeliefert\\.$" +
            "|^\\[Transport] Du hast ein Weizen Paket abgeliefert\\.$" +
            "|^\\[Transport] Du hast eine Schwarzpulverkiste abgeliefert\\.$");
    private static final Pattern DRINK_TRANSPORT_DELIVER_PATTERN = compile("^\\[Bar] Du hast eine Flasche abgegeben!$");
    private static final Pattern TABAK_JOB_TRANSPORT_START_PATTERN = compile("^\\[Tabakplantage] Bringe es nun zur Shishabar und gib es mit /droptabak ab\\.$");

    private boolean isTabakJobTransportActive = false;

    @Override
    public boolean onMessageReceive(String message) {
        Matcher transportDeliverMatcher = TRANSPORT_DELIVER_PATTERN.matcher(message);
        if (transportDeliverMatcher.find()) {
            delayedAction(() -> networkHandler.sendChatCommand("droptransport"), SECONDS.toMillis(10));
            return true;
        }

        Matcher drinkTransportDeliverMatcher = DRINK_TRANSPORT_DELIVER_PATTERN.matcher(message);
        if (drinkTransportDeliverMatcher.find()) {
            delayedAction(() -> networkHandler.sendChatCommand("dropdrink"), 2500);
            return true;
        }

        Matcher tabakJobTransportStartMatcher = TABAK_JOB_TRANSPORT_START_PATTERN.matcher(message);
        if (tabakJobTransportStartMatcher.find()) {
            this.isTabakJobTransportActive = true;
            return true;
        }

        return true;
    }

    @Override
    public void onNaviSpotReached() {
        if (this.isTabakJobTransportActive && player.getBlockPos().isWithinDistance(new BlockPos(-133, 69, -78), 3)) {
            networkHandler.sendChatCommand("droptabak");
            this.isTabakJobTransportActive = false;
        }
    }
}
