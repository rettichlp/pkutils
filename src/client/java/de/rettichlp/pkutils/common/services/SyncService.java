package de.rettichlp.pkutils.common.services;

import de.rettichlp.pkutils.common.manager.PKUtilsBase;
import de.rettichlp.pkutils.common.storage.schema.Faction;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.function.Consumer;

import static de.rettichlp.pkutils.PKUtilsClient.networkHandler;
import static de.rettichlp.pkutils.PKUtilsClient.player;
import static de.rettichlp.pkutils.PKUtilsClient.storage;
import static de.rettichlp.pkutils.common.storage.schema.Faction.FBI;
import static de.rettichlp.pkutils.common.storage.schema.Faction.NULL;
import static de.rettichlp.pkutils.common.storage.schema.Faction.POLIZEI;
import static java.time.LocalDateTime.MIN;
import static java.time.LocalDateTime.now;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

@Getter
@Setter
public class SyncService extends PKUtilsBase {

    private LocalDateTime lastSyncTimestamp = MIN;
    private boolean gameSyncProcessActive = false;

    public void executeSync() {
        this.gameSyncProcessActive = true;
        sendModMessage("PKUtils wird synchronisiert...", false);

        // seconds 1-13: execute commands for all factions -> blocks command input for 13 * 1000 ms
        for (Faction faction : Faction.values()) {
            if (faction == NULL) {
                continue;
            }

            delayedAction(() -> networkHandler.sendChatCommand("memberinfoall " + faction.getMemberInfoCommandName()), 1000 * faction.ordinal());
        }

        // second 13: faction-related init commands
        delayedAction(() -> {
            Faction faction = storage.getFaction(requireNonNull(player.getDisplayName()).getString());

            if (faction.isBadFaction()) {
                networkHandler.sendChatCommand("blacklist");
            } else if (faction == FBI || faction == POLIZEI) {
                networkHandler.sendChatCommand("wanteds");
            }
        }, Faction.values().length * 1000L);

        // end: init commands dons
        delayedAction(() -> {
            this.gameSyncProcessActive = false;
            sendModMessage("PKUtils synchronisiert.", false);
            this.lastSyncTimestamp = now();
        }, Faction.values().length * 1000L + 200);
    }

    public void retrieveNumberAndRun(String playerName, Consumer<Integer> runWithNumber) {
        networkHandler.sendChatCommand("nummer " + playerName);

        delayedAction(() -> {
            ofNullable(storage.getRetrievedNumbers().get(playerName)).ifPresentOrElse(runWithNumber, () -> {
                sendModMessage("Die Nummer von " + playerName + " konnte nicht abgerufen werden.", false);
            });
        }, 1000);
    }
}
