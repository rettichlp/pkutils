package de.rettichlp.common.storage;

import de.rettichlp.common.storage.schema.Faction;
import de.rettichlp.common.storage.schema.FactionMember;
import de.rettichlp.common.storage.schema.WantedEntry;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.rettichlp.PKUtils.LOGGER;

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

    public void resetFactionMembers() {
        for (Faction faction : Faction.values()) {
            resetFactionMembers(faction);
        }
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
