package de.rettichlp.common.storage.schema;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class FactionMember {

    private final String playerName;
    private final int rank;
}
