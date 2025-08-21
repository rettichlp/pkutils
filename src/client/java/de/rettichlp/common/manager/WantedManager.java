package de.rettichlp.common.manager;

import de.rettichlp.common.listener.JoinListener;
import de.rettichlp.common.listener.MessageListener;
import de.rettichlp.common.storage.schema.WantedEntry;
import lombok.NoArgsConstructor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.PKUtilsClient.networkHandler;
import static de.rettichlp.PKUtilsClient.storage;
import static java.lang.Integer.parseInt;
import static java.lang.System.currentTimeMillis;
import static java.util.regex.Pattern.compile;

@NoArgsConstructor
public class WantedManager extends BaseManager implements JoinListener, MessageListener {

    private final static Pattern ONLINE_WANTED_PLAYERS_HEADER_PATTERN = compile("Online Spieler mit WantedPunkten:");
    private final static Pattern ONLINE_WANTED_PLAYERS_ENTRY_PATTERN = compile("- (?<playerName>[A-Za-z0-9_]+) \\| (?<wantedPointAmount>\\d+) \\| (?<reason>.+)(?<afk> \\| AFK|)");

    private long activeWantedCheck = 0;

    @Override
    public void onJoin() {
        delayedAction(() -> networkHandler.sendChatCommand("wanteds"), 15000);
    }

    @Override
    public void onMessage(String message) {
        Matcher onlineWantedPlayersHeaderMatcher = ONLINE_WANTED_PLAYERS_HEADER_PATTERN.matcher(message);
        if (onlineWantedPlayersHeaderMatcher.find()) {
            this.activeWantedCheck = currentTimeMillis();
            storage.resetWantedEntries();
            return;
        }

        Matcher onlineWantedPlayersEntryMatcher = ONLINE_WANTED_PLAYERS_ENTRY_PATTERN.matcher(message);
        if (onlineWantedPlayersEntryMatcher.find() && (currentTimeMillis() - this.activeWantedCheck < 2000)) {
            String playerName = onlineWantedPlayersEntryMatcher.group("playerName");
            int wantedPointAmount = parseInt(onlineWantedPlayersEntryMatcher.group("wantedPointAmount"));
            String reason = onlineWantedPlayersEntryMatcher.group("reason");

            WantedEntry wantedEntry = new WantedEntry(playerName, wantedPointAmount, reason);
            storage.addWantedEntry(wantedEntry);
        }
    }
}
