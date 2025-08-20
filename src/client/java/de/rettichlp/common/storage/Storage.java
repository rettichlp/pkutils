package de.rettichlp.common.storage;

import de.rettichlp.common.storage.schema.WantedEntry;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

import static de.rettichlp.PKUtils.LOGGER;

public class Storage {

    @Getter
    private final List<WantedEntry> wantedEntries = new ArrayList<>();

    public void print() {
        LOGGER.info("wantedEntries[{}]: {}", this.wantedEntries.size(), this.wantedEntries);
    }

    public void addWantedEntry(WantedEntry entry) {
        this.wantedEntries.add(entry);
    }

    public void resetWantedEntries() {
        this.wantedEntries.clear();
    }
}
