package de.rettichlp.pkutils.common.storage.schema;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class BlacklistEntry {

    private final String playerName;
    private final String reason;
    private final boolean outlaw;
    private final int kills;
    private final int price;
}
