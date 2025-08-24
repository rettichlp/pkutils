package de.rettichlp.pkutils.common.manager;

import de.rettichlp.pkutils.common.listener.IMessageReceiveListener;
import de.rettichlp.pkutils.common.storage.schema.BlacklistEntry;
import de.rettichlp.pkutils.common.storage.schema.Faction;
import de.rettichlp.pkutils.common.storage.schema.FactionMember;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.pkutils.PKUtils.LOGGER;
import static de.rettichlp.pkutils.PKUtilsClient.networkHandler;
import static de.rettichlp.pkutils.PKUtilsClient.player;
import static de.rettichlp.pkutils.PKUtilsClient.storage;
import static de.rettichlp.pkutils.common.storage.schema.Faction.FBI;
import static de.rettichlp.pkutils.common.storage.schema.Faction.NULL;
import static de.rettichlp.pkutils.common.storage.schema.Faction.POLIZEI;
import static de.rettichlp.pkutils.common.storage.schema.Faction.fromDisplayName;
import static java.lang.Integer.parseInt;
import static java.lang.System.currentTimeMillis;
import static java.time.LocalDateTime.MIN;
import static java.time.LocalDateTime.now;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.regex.Pattern.compile;

@NoArgsConstructor
public class SyncManager extends BaseManager implements IMessageReceiveListener {

    private static final Pattern SERVER_PASSWORD_MISSING_PATTERN = compile("^» Schütze deinen Account mit /passwort new \\[Passwort]$");
    private static final Pattern SERVER_PASSWORD_ACCEPTED_PATTERN = compile("^Du hast deinen Account freigeschaltet\\.$");
    private static final Pattern FACTION_MEMBER_ALL_HEADER = compile("^={4} Mitglieder von (?<factionName>.+) ={4}$");
    private static final Pattern FACTION_MEMBER_ALL_ENTRY = compile("^\\s*-\\s*(?<rank>\\d)\\s*\\|\\s*(?<playerNames>.+)$");
    private static final Pattern BLACKLIST_HEADER_PATTERN = compile("^==== Blacklist .+ ====$");
    private static final Pattern BLACKLIST_ENTRY_PATTERN = compile("^ » (?<playerName>[a-zA-Z0-9_]+) \\| (?<reason>.+) \\| (?<dateTime>.+) \\| (?<kills>\\d+) Kills \\| (?<price>\\d+)\\$(| \\(AFK\\))$");

    @Getter
    private LocalDateTime lastSyncTimestamp = MIN;
    @Getter
    private boolean gameSyncProcessActive = false;
    private Faction factionMemberRetrievalFaction;
    private long factionMemberRetrievalTimestamp;
    private long activeCheck = 0;

    @Override
    public boolean onMessageReceive(String message) {
        // SERVER INIT

        // if a password is not set, start the game sync process
        Matcher passwordMissingMatcher = SERVER_PASSWORD_MISSING_PATTERN.matcher(message);
        if (passwordMissingMatcher.find()) {
            executeSync();
            return true;
        }

        // if a password is accepted, start the game sync process
        Matcher passwordAcceptedMatcher = SERVER_PASSWORD_ACCEPTED_PATTERN.matcher(message);
        if (passwordAcceptedMatcher.find()) {
            executeSync();
            return true;
        }

        // FACTION ALL INIT

        Matcher factionMemberAllHeaderMatcher = FACTION_MEMBER_ALL_HEADER.matcher(message);
        if (factionMemberAllHeaderMatcher.find()) {
            String factionName = factionMemberAllHeaderMatcher.group("factionName");
            this.factionMemberRetrievalTimestamp = currentTimeMillis();
            this.factionMemberRetrievalFaction = fromDisplayName(factionName)
                    .orElseThrow(() -> new IllegalStateException("Could not find faction with name: " + factionName));

            storage.resetFactionMembers(this.factionMemberRetrievalFaction);
            return !this.gameSyncProcessActive;
        }

        Matcher factionMemberAllEntryMatcher = FACTION_MEMBER_ALL_ENTRY.matcher(message);
        if (factionMemberAllEntryMatcher.find() && (currentTimeMillis() - this.factionMemberRetrievalTimestamp < 500) && nonNull(this.factionMemberRetrievalFaction)) {
            int rank = parseInt(factionMemberAllEntryMatcher.group("rank"));
            String[] playerNames = factionMemberAllEntryMatcher.group("playerNames")
                    .split(", ");

            for (String playerName : playerNames) {
                FactionMember factionMember = new FactionMember(playerName, rank);
                storage.addFactionMember(this.factionMemberRetrievalFaction, factionMember);
            }

            return !this.gameSyncProcessActive;
        }

        // FACTION SPECIFIC INIT - POLICE - WANTED PLAYERS -> WantedManager

        // FACTION SPECIFIC INIT - BAD FRAK - BLACKLIST

        Matcher blacklistHeaderMatcher = BLACKLIST_HEADER_PATTERN.matcher(message);
        if (blacklistHeaderMatcher.find()) {
            this.activeCheck = currentTimeMillis();
            storage.resetBlacklistEntries();
            return !this.gameSyncProcessActive;
        }

        Matcher blacklistEntryMatcher = BLACKLIST_ENTRY_PATTERN.matcher(message);
        if (blacklistEntryMatcher.find() && (currentTimeMillis() - this.activeCheck < 100)) {
            String playerName = blacklistEntryMatcher.group("playerName");
            String reason = blacklistEntryMatcher.group("reason");
            boolean outlaw = reason.toLowerCase().contains("(vf)") || reason.toLowerCase().contains("(vogelfrei)");
            int kills = parseInt(blacklistEntryMatcher.group("kills"));
            int price = parseInt(blacklistEntryMatcher.group("price"));

            BlacklistEntry blacklistEntry = new BlacklistEntry(playerName, reason, outlaw, kills, price);
            storage.addBlacklistEntry(blacklistEntry);
            return !this.gameSyncProcessActive;
        }

        return true;
    }

    public void executeSync() {
        this.gameSyncProcessActive = true;
        sendModMessage("PKUtils wird synchronisiert...", false);

        // seconds 1-13: execute commands for all factions -> blocks command input for 13 * 1000 ms
        for (Faction faction : Faction.values()) {
            if (faction == NULL) {
                continue;
            }

            delayedAction(() -> networkHandler.sendChatCommand("memberinfoall " + faction.getMemberInfoCommandName()), 1000 * faction.ordinal());
        }

        // second 13: faction-related init commands
        delayedAction(() -> {
            Faction faction = storage.getFaction(requireNonNull(player.getDisplayName()).getString());
            switch (faction) {
                case POLIZEI -> networkHandler.sendChatCommand("wanteds"); // TODO duty check
                case CALDERON, KERZAKOV, LACOSANOSTRA, LEMILIEU, WESTSIDEBALLAS -> networkHandler.sendChatCommand("blacklist");
            }
        },  Faction.values().length * 1000L);

        // end: init commands dons
        delayedAction(() -> {
            this.gameSyncProcessActive = false;
            sendModMessage("PKUtils synchronisiert.", false);
            this.lastSyncTimestamp = now();
        }, Faction.values().length * 1000L + 200);
    }
}
