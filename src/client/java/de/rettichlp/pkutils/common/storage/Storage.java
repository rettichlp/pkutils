package de.rettichlp.pkutils.common.storage;

import de.rettichlp.pkutils.common.storage.schema.Faction;
import de.rettichlp.pkutils.common.storage.schema.FactionMember;
import de.rettichlp.pkutils.common.storage.schema.WantedEntry;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.rettichlp.pkutils.PKUtils.LOGGER;
import static de.rettichlp.pkutils.common.storage.schema.Faction.NULL;

public class Storage {

    @Getter
    private final Map<Faction, Set<FactionMember>> factionMembers = new HashMap<>();

    @Getter
    private final List<WantedEntry> wantedEntries = new ArrayList<>();

    public void print() {
        // factionMembers
        this.factionMembers.forEach((faction, factionMembers) -> LOGGER.info("factionMembers[{}:{}]: {}", faction, factionMembers.size(), factionMembers));
        // wantedEntries
        LOGGER.info("wantedEntries[{}]: {}", this.wantedEntries.size(), this.wantedEntries);
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
}
