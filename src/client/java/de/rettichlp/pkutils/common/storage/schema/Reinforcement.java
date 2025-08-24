package de.rettichlp.pkutils.common.storage.schema;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static de.rettichlp.pkutils.PKUtilsClient.storage;
import static de.rettichlp.pkutils.common.storage.schema.Faction.POLIZEI;
import static de.rettichlp.pkutils.common.storage.schema.Reinforcement.Status.*;
import static de.rettichlp.pkutils.common.storage.schema.Reinforcement.Status.ACCEPTED;
import static de.rettichlp.pkutils.common.storage.schema.Reinforcement.Status.AFK;
import static de.rettichlp.pkutils.common.storage.schema.Reinforcement.Status.WENT_AFK;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;

@Data
@AllArgsConstructor
public class Reinforcement {

    private Type type;
    private final String playerName;
    private final Map<String, Status> statusMap;
    private final LocalDateTime time = LocalDateTime.now();

    public Reinforcement(Type type, String playerName) {
        this.type = type;
        this.playerName = playerName;
        this.statusMap = storage.getFactionMembers(POLIZEI).stream()
                .collect(toMap(FactionMember::getPlayerName, factionMember -> {
                    if (!factionMember.isDuty()) {
                        return NOT_DUTY;
                    }

                    if (factionMember.isAfk()) {
                        return AFK;
                    }

                    return ACTIVE;
                }));
    }

    public void setAccepted(String playerName) {
        this.statusMap.put(playerName, ACCEPTED);
    }

    public void setWentAfk(String playerName) {
        this.statusMap.put(playerName, WENT_AFK);
    }

    @Getter
    @AllArgsConstructor
    public enum Type {

        DEFAULT("-f", null),
        D_CHAT("-d", null),
        RAM("-r", "Rammen!"),
        RAM_D("-rd", "Rammen!"),
        EMERGENCY("-e", "Dringend!"),
        EMERGENCY_D("-ed", "Dringend!"),
        MEDIC("-m", "Medic benötigt!"),
        CORPSE_GUARDING("-lb", "Leichenbewachung!"),
        DRUG_REMOVAL("-da", "Drogenabnahme!"),
        CONTRACT("-ct", "Contract!"),
        PLANT("-p", "Plant!"),
        BOMB("-b", "Bombe!"),
        HOSTAGE_TAKING("-gn", "Geiselnahme!"),
        HOSTAGE_TAKING_D("-gnd", "Geiselnahme!"),
        TRAINING("-t", "Training!"),
        TRAINING_D("-td", "Training!"),
        TEST("-test", "Test!");

        private final String argument;
        private final String message;

        public static @NotNull Optional<Type> fromArgument(String argument) {
            return stream(values())
                    .filter(type -> type.getArgument().equals(argument))
                    .findFirst();
        }
    }

    public enum Status {

        ACTIVE, // blue
        AFK, // dark_gray
        NOT_DUTY, // gray
        ACCEPTED, // green
        WENT_AFK // gold
    }
}
