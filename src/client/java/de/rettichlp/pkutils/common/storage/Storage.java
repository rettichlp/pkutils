package de.rettichlp.pkutils.common.storage;

import de.rettichlp.pkutils.common.storage.schema.BlacklistEntry;
import de.rettichlp.pkutils.common.storage.schema.Faction;
import de.rettichlp.pkutils.common.storage.schema.FactionMember;
import de.rettichlp.pkutils.common.storage.schema.WantedEntry;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.rettichlp.pkutils.PKUtils.LOGGER;
import static de.rettichlp.pkutils.common.storage.Storage.ToggledChat.NONE;
import static de.rettichlp.pkutils.common.storage.schema.Faction.NULL;

public class Storage {

    @Getter
    private final Map<Faction, Set<FactionMember>> factionMembers = new HashMap<>();

    @Getter
    private final List<BlacklistEntry> blacklistEntries = new ArrayList<>();

    @Getter
    private final List<WantedEntry> wantedEntries = new ArrayList<>();

    @Getter
    private final Map<String, Integer> retrievedNumbers = new HashMap<>();

    @Getter
    @Setter
    private ToggledChat toggledChat = NONE;

    public void print() {
        // factionMembers
        this.factionMembers.forEach((faction, factionMembers) -> LOGGER.info("factionMembers[{}:{}]: {}", faction, factionMembers.size(), factionMembers));
        // blacklistEntries
        LOGGER.info("blacklistEntries[{}]: {}", this.blacklistEntries.size(), this.blacklistEntries);
        // wantedEntries
        LOGGER.info("wantedEntries[{}]: {}", this.wantedEntries.size(), this.wantedEntries);
    }

    public void addBlacklistEntry(BlacklistEntry entry) {
        this.blacklistEntries.add(entry);
    }

    public void resetBlacklistEntries() {
        this.blacklistEntries.clear();
    }

    public void addFactionMember(Faction faction, FactionMember factionMember) {
        this.factionMembers.computeIfAbsent(faction, f -> new HashSet<>())
                .add(factionMember);
    }

    public Set<FactionMember> getFactionMembers(Faction faction) {
        return this.factionMembers.getOrDefault(faction, new HashSet<>());
    }

    public Faction getFaction(String playerName) {
        return this.factionMembers.entrySet().stream()
                .filter(entry -> entry.getValue().stream()
                        .anyMatch(factionMember -> factionMember.getPlayerName().equals(playerName)))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(NULL);
    }

    public void resetFactionMembers(Faction faction) {
        this.factionMembers.put(faction, new HashSet<>());
    }

    public void addWantedEntry(WantedEntry entry) {
        this.wantedEntries.add(entry);
    }

    public void resetWantedEntries() {
        this.wantedEntries.clear();
    }

    @Getter
    @AllArgsConstructor
    public enum ToggledChat {

        NONE("", "Dauerhafter Fraktionschat deaktiviert."),
        D_CHAT("d", "Dauerhafter D-Chat aktiviert."),
        F_CHAT("f", "Dauerhafter F-Chat aktiviert.");

        private final String command;
        private final String toggleMessage;
    }
}
