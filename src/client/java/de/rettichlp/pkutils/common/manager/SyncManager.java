package de.rettichlp.pkutils.common.manager;

import de.rettichlp.pkutils.common.listener.IMessageReceiveListener;
import de.rettichlp.pkutils.common.storage.schema.BlacklistEntry;
import de.rettichlp.pkutils.common.storage.schema.Faction;
import de.rettichlp.pkutils.common.storage.schema.FactionMember;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.pkutils.PKUtils.LOGGER;
import static de.rettichlp.pkutils.PKUtilsClient.networkHandler;
import static de.rettichlp.pkutils.PKUtilsClient.player;
import static de.rettichlp.pkutils.PKUtilsClient.storage;
import static de.rettichlp.pkutils.common.storage.schema.Faction.NULL;
import static de.rettichlp.pkutils.common.storage.schema.Faction.fromDisplayName;
import static java.lang.Integer.parseInt;
import static java.lang.System.currentTimeMillis;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.regex.Pattern.compile;

@NoArgsConstructor
public class SyncManager extends BaseManager implements IMessageReceiveListener {

    private static final Pattern SERVER_WELCOME_BACK_PATTERN = compile("^Willkommen zurück!$");
    private static final Pattern SERVER_PASSWORD_REQUIRED_PATTERN = compile("^Schalte deinen Account frei mit /passwort \\[Passwort]$");
    private static final Pattern SERVER_PASSWORD_ACCEPTED_PATTERN = compile("^Du hast deinen Account freigeschaltet\\.$");
    private static final Pattern FACTION_MEMBER_ALL_HEADER = compile("^={4} Mitglieder von (?<factionName>.+) ={4}$");
    private static final Pattern FACTION_MEMBER_ALL_ENTRY = compile("^\\s*-\\s*(?<rank>\\d)\\s*\\|\\s*(?<playerNames>.+)$");
    private static final Pattern BLACKLIST_HEADER_PATTERN = compile("^==== Blacklist .+ ====$");
    private static final Pattern BLACKLIST_ENTRY_PATTERN = compile("^ » (?<playerName>[a-zA-Z0-9_]+) \\| (?<reason>.+) \\| (?<dateTime>.+) \\| (?<kills>\\d+) Kills \\| (?<price>\\d+)\\$(| \\(AFK\\))$");

    @Getter
    private boolean gameSyncProcessActive = false;
    private boolean gameSyncProcessScheduled = false;
    private Faction factionMemberRetrievalFaction;
    private long factionMemberRetrievalTimestamp;
    private long activeCheck = 0;

    @Override
    public boolean onMessageReceive(String message) {
        // SERVER INIT

        // schedule the game sync process if a join message is received
        Matcher welcomeBackMatcher = SERVER_WELCOME_BACK_PATTERN.matcher(message);
        if (welcomeBackMatcher.find()) {
            this.gameSyncProcessScheduled = true;
            // execute the sync process after a delay to stop it if a password is required
            delayedAction(this::executeSyncCommands, 1000);
            return true;
        }

        // if a password is required, stop the game sync process
        Matcher passwordRequiredMatcher = SERVER_PASSWORD_REQUIRED_PATTERN.matcher(message);
        if (passwordRequiredMatcher.find()) {
            // stop the game sync process
            this.gameSyncProcessScheduled = false;
            return true;
        }

        // if a password is accepted, start the game sync process again (without delay)
        Matcher passwordAcceptedMatcher = SERVER_PASSWORD_ACCEPTED_PATTERN.matcher(message);
        if (passwordAcceptedMatcher.find()) {
            this.gameSyncProcessScheduled = true;
            executeSyncCommands();
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
        this.gameSyncProcessScheduled = true;
        executeSyncCommands();
    }

    private void executeSyncCommands() {
        if (!this.gameSyncProcessScheduled) {
            LOGGER.info("Game sync process is not scheduled, skipping...");
            return;
        }

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
        }, Faction.values().length * 1000L + 200);
    }
}
