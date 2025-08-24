package de.rettichlp.pkutils.common.storage.schema;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import net.minecraft.client.MinecraftClient;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;

import java.util.List;

import static java.util.Optional.ofNullable;

@Getter
@ToString
@AllArgsConstructor
public class FactionMember {

    private final String playerName;
    private final int rank;

    public boolean isAfk() {
        assert MinecraftClient.getInstance().world != null; // cannot be null at this point
        Scoreboard scoreboard = MinecraftClient.getInstance().world.getScoreboard();
        return ofNullable(scoreboard.getTeam("AFK"))
                .map(Team::getPlayerList)
                .map(playerNames -> playerNames.contains(this.playerName))
                .orElse(false);
    }

    public boolean isDuty() {
        assert MinecraftClient.getInstance().world != null; // cannot be null at this point
        Scoreboard scoreboard = MinecraftClient.getInstance().world.getScoreboard();
        List<String> teamNames = scoreboard.getTeams().stream()
                .filter(team -> team.getPlayerList().contains(this.playerName))
                .map(Team::getName)
                .toList();

        return teamNames.contains("001_fbi") || teamNames.contains("002_polizei") || teamNames.contains("003_medic") || teamNames.contains("004_news");
    }
}
