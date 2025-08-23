package de.rettichlp.pkutils.common.storage.schema;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WantedEntry {

    private String playerName;
    private int wantedPointAmount;
    private String reason;
}
