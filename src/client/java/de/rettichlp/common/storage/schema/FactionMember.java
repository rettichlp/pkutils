package de.rettichlp.common.storage.schema;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FactionMember {

    private final String playerName;
    private final int rank;
}
