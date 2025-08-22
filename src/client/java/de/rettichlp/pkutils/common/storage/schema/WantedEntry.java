package de.rettichlp.pkutils.common.storage.schema;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class WantedEntry {

    private final String playerName;
    private final int wantedPointAmount;
    private final String reason;
}
