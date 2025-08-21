package de.rettichlp.common.manager;

import de.rettichlp.common.listener.JoinListener;
import de.rettichlp.common.listener.MessageListener;
import de.rettichlp.common.storage.schema.Faction;
import de.rettichlp.common.storage.schema.FactionMember;
import lombok.NoArgsConstructor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.PKUtilsClient.networkHandler;
import static de.rettichlp.PKUtilsClient.storage;
import static de.rettichlp.common.storage.schema.Faction.NULL;
import static de.rettichlp.common.storage.schema.Faction.fromFactionKey;
import static java.lang.Integer.parseInt;
import static java.lang.System.currentTimeMillis;
import static java.util.Objects.nonNull;
import static java.util.regex.Pattern.compile;

@NoArgsConstructor
public class FactionManager extends BaseManager implements JoinListener, MessageListener {

    private static final Pattern FACTION_MEMBER_ALL_HEADER = compile("^={4} Mitglieder von (?<factionName>.+) ={4}$");
    private static final Pattern FACTION_MEMBER_ALL_ENTRY = compile("^\\s*-\\s*(?<rank>\\d)\\s*\\|\\s*(?<playerNames>.+)$");

    private Faction factionMemberRetrievalFaction;
    private long factionMemberRetrievalTimestamp;

    @Override
    public void onJoin() {
        for (Faction faction : Faction.values()) {
            if (faction == NULL) {
                continue;
            }

            delayedAction(() -> networkHandler.sendChatCommand("memberinfoall " + faction.getFactionKey()), 1000 * faction.ordinal() + 1000);
        }
    }

    @Override
    public void onMessage(String message) {
        Matcher factionMemberAllHeaderMatcher = FACTION_MEMBER_ALL_HEADER.matcher(message);
        if (factionMemberAllHeaderMatcher.find()) {
            String factionName = factionMemberAllHeaderMatcher.group("factionName");
            this.factionMemberRetrievalTimestamp = currentTimeMillis();
            this.factionMemberRetrievalFaction = fromFactionKey(factionName)
                    .orElseThrow(() -> new IllegalStateException("Could not find faction with name: " + factionName));

            storage.resetFactionMembers(this.factionMemberRetrievalFaction);
            return;
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
        }
    }
}
