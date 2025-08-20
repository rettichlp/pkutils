package de.rettichlp.common.manager;

import de.rettichlp.common.listener.MessageListener;
import de.rettichlp.common.storage.schema.WantedEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.PKUtilsClient.storage;
import static java.lang.Integer.parseInt;
import static java.lang.System.currentTimeMillis;
import static java.util.Optional.ofNullable;
import static java.util.regex.Pattern.compile;

public class WantedManager implements MessageListener {

    private final static Pattern WELCOME_BACK_PATTERN = compile("Willkommen zurück!");
    private final static Pattern ONLINE_WANTED_PLAYERS_HEADER_PATTERN = compile("Online Spieler mit WantedPunkten:");
    private final static Pattern ONLINE_WANTED_PLAYERS_ENTRY_PATTERN = compile("- (?<playerName>[A-Za-z0-9_]+) \\| (?<wantedPointAmount>\\d+) \\| (?<reason>.+)(?<afk> \\| AFK|)");

    private final ClientPlayNetworkHandler networkHandler;

    private long activeWantedCheck = 0;

    public WantedManager() {
        this.networkHandler = ofNullable(MinecraftClient.getInstance().player)
                .map(clientPlayerEntity -> clientPlayerEntity.networkHandler)
                .orElseThrow();
    }

    @Override
    public void onMessage(String message) {
        Matcher welcomeBackMatcher = WELCOME_BACK_PATTERN.matcher(message);
        if (welcomeBackMatcher.find()) {
            this.networkHandler.sendChatCommand("wanteds");
            return;
        }

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
