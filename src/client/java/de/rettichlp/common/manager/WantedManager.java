package de.rettichlp.common.manager;

import de.rettichlp.common.listener.JoinListener;
import de.rettichlp.common.listener.MessageListener;
import de.rettichlp.common.storage.schema.Faction;
import de.rettichlp.common.storage.schema.WantedEntry;
import lombok.NoArgsConstructor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.PKUtilsClient.networkHandler;
import static de.rettichlp.PKUtilsClient.player;
import static de.rettichlp.PKUtilsClient.storage;
import static de.rettichlp.common.storage.schema.Faction.POLIZEI;
import static java.lang.Integer.parseInt;
import static java.lang.System.currentTimeMillis;
import static java.util.Objects.requireNonNull;
import static java.util.regex.Pattern.compile;

@NoArgsConstructor
public class WantedManager extends BaseManager implements JoinListener, MessageListener {

    private final static Pattern ONLINE_WANTED_PLAYERS_HEADER_PATTERN = compile("Online Spieler mit WantedPunkten:");
    private final static Pattern ONLINE_WANTED_PLAYERS_ENTRY_PATTERN = compile("- (?<playerName>[A-Za-z0-9_]+) \\| (?<wantedPointAmount>\\d+) \\| (?<reason>.+)(?<afk> \\| AFK|)");

    private long activeWantedCheck = 0;
    private boolean showMessage = true;

    @Override
    public void onJoin() {
        delayedAction(() -> {
            Faction faction = storage.getFaction(requireNonNull(player.getDisplayName()).getString());
            if (faction == POLIZEI) {
                networkHandler.sendChatCommand("wanteds");
                this.showMessage = false;
            }
        }, 15000);
    }

    @Override
    public boolean onMessage(String message) {
        Matcher onlineWantedPlayersHeaderMatcher = ONLINE_WANTED_PLAYERS_HEADER_PATTERN.matcher(message);
        if (onlineWantedPlayersHeaderMatcher.find()) {
            this.activeWantedCheck = currentTimeMillis();
            storage.resetWantedEntries();
            delayedAction(() -> this.showMessage = true, 500);
            return this.showMessage;
        }

        Matcher onlineWantedPlayersEntryMatcher = ONLINE_WANTED_PLAYERS_ENTRY_PATTERN.matcher(message);
        if (onlineWantedPlayersEntryMatcher.find() && (currentTimeMillis() - this.activeWantedCheck < 2000)) {
            String playerName = onlineWantedPlayersEntryMatcher.group("playerName");
            int wantedPointAmount = parseInt(onlineWantedPlayersEntryMatcher.group("wantedPointAmount"));
            String reason = onlineWantedPlayersEntryMatcher.group("reason");

            WantedEntry wantedEntry = new WantedEntry(playerName, wantedPointAmount, reason);
            storage.addWantedEntry(wantedEntry);
            return this.showMessage;
        }

        return true;
    }
}
