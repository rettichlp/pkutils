package de.rettichlp.pkutils.common.manager;

import de.rettichlp.pkutils.common.registry.PKUtilsListener; // NEUER IMPORT
import de.rettichlp.pkutils.common.storage.schema.Faction;
import de.rettichlp.pkutils.common.storage.schema.FactionMember;
import de.rettichlp.pkutils.listener.IMessageReceiveListener;
import lombok.NoArgsConstructor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.pkutils.PKUtilsClient.storage;
import static de.rettichlp.pkutils.PKUtilsClient.syncService;
import static de.rettichlp.pkutils.common.storage.schema.Faction.fromDisplayName;
import static java.lang.Integer.parseInt;
import static java.lang.System.currentTimeMillis;
import static java.util.Objects.nonNull;
import static java.util.regex.Pattern.compile;

@NoArgsConstructor
@PKUtilsListener
public class SyncManager extends PKUtilsBase implements IMessageReceiveListener {

    private static final Pattern SERVER_PASSWORD_MISSING_PATTERN = compile("^» Schütze deinen Account mit /passwort new \\[Passwort]$");
    private static final Pattern SERVER_PASSWORD_ACCEPTED_PATTERN = compile("^Du hast deinen Account freigeschaltet\\.$");
    private static final Pattern FACTION_MEMBER_ALL_HEADER = compile("^={4} Mitglieder von (?<factionName>.+) ={4}$");
    private static final Pattern FACTION_MEMBER_ALL_ENTRY = compile("^\\s*-\\s*(?<rank>\\d)\\s*\\|\\s*(?<playerNames>.+)$");

    private Faction factionMemberRetrievalFaction;
    private long factionMemberRetrievalTimestamp;

    @Override
    public boolean onMessageReceive(String message) {

        Matcher passwordMissingMatcher = SERVER_PASSWORD_MISSING_PATTERN.matcher(message);
        if (passwordMissingMatcher.find()) {
            syncService.executeSync();
            return true;
        }

        Matcher passwordAcceptedMatcher = SERVER_PASSWORD_ACCEPTED_PATTERN.matcher(message);
        if (passwordAcceptedMatcher.find()) {
            syncService.executeSync();
            return true;
        }


        Matcher factionMemberAllHeaderMatcher = FACTION_MEMBER_ALL_HEADER.matcher(message);
        if (factionMemberAllHeaderMatcher.find()) {
            String factionName = factionMemberAllHeaderMatcher.group("factionName");
            this.factionMemberRetrievalTimestamp = currentTimeMillis();
            this.factionMemberRetrievalFaction = fromDisplayName(factionName)
                    .orElseThrow(() -> new IllegalStateException("Could not find faction with name: " + factionName));

            storage.resetFactionMembers(this.factionMemberRetrievalFaction);
            return !syncService.isGameSyncProcessActive();
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

            return !syncService.isGameSyncProcessActive();
        }

        return true;
    }
}