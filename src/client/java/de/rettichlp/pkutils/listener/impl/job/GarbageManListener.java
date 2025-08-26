package de.rettichlp.pkutils.listener.impl.job;

import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import de.rettichlp.pkutils.common.registry.PKUtilsListener;
import de.rettichlp.pkutils.listener.IMessageReceiveListener;
import de.rettichlp.pkutils.listener.ITickListener;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.pkutils.PKUtilsClient.networkHandler;
import static de.rettichlp.pkutils.PKUtilsClient.player;
import static java.lang.Double.compare;
import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.regex.Pattern.compile;
import static net.minecraft.scoreboard.ScoreboardDisplaySlot.SIDEBAR;

@PKUtilsListener
public class GarbageManListener extends PKUtilsBase implements IMessageReceiveListener, ITickListener {

    private static final Pattern GARBAGE_MAN_DROP_START = compile("^\\[Müllmann] Hier kannst du auf den Haufen mit /dropwaste dein Müll sortieren!$");
    private static final Pattern GARBAGE_MAN_FINISHED = compile("^\\[Müllmann] Du hast den Job beendet\\.$");

    private boolean isDropStep = false;
    private long lastCommandExecution = 0;

    @Override
    public boolean onMessageReceive(String message) {
        Matcher garbageManDropStartMatcher = GARBAGE_MAN_DROP_START.matcher(message);
        if (garbageManDropStartMatcher.find()) {
            this.isDropStep = true;
            return true;
        }

        Matcher garbageManFinishedMatcher = GARBAGE_MAN_FINISHED.matcher(message);
        if (garbageManFinishedMatcher.find()) {
            this.isDropStep = false;
            return true;
        }

        return true;
    }

    @Override
    public void onTick() {
        // check if drop step
        if (!this.isDropStep) {
            return;
        }

        // check if in drop spot range
        WasteDropSpot nearestWasteDropSpot = getNearestWasteDropSpot();
        if (!player.getBlockPos().isWithinDistance(nearestWasteDropSpot.getDropSpot(), 3)) {
            return;
        }

        // check if waste left
        int wasteLeft = getWasteLeft(nearestWasteDropSpot);
        if (wasteLeft <= 0) {
            return;
        }

        // check dropwaste command cooldown
        long now = currentTimeMillis();
        if (now - this.lastCommandExecution < 5200) {
            return;
        }

        this.lastCommandExecution = now;
        networkHandler.sendChatCommand("dropwaste");

        delayedAction(() -> sendModMessage("5", true), 200);
        delayedAction(() -> sendModMessage("4", true), 1200);
        delayedAction(() -> sendModMessage("3", true), 2200);
        delayedAction(() -> sendModMessage("2", true), 3200);
        delayedAction(() -> sendModMessage("1", true), 4200);
    }

    private WasteDropSpot getNearestWasteDropSpot() {
        return stream(WasteDropSpot.values()).min((o1, o2) -> {
            BlockPos blockPos = player.getBlockPos();
            double distance1 = blockPos.getSquaredDistance(o1.getDropSpot());
            double distance2 = blockPos.getSquaredDistance(o2.getDropSpot());
            return compare(distance1, distance2);
        }).orElseThrow(() -> new IllegalStateException("This should never happen"));
    }

    private int getWasteLeft(WasteDropSpot wasteDropSpot) {
        assert MinecraftClient.getInstance().world != null; // cannot be null at this point
        Scoreboard scoreboard = MinecraftClient.getInstance().world.getScoreboard();

        Collection<ScoreboardEntry> scoreboardEntries = getGarbageManScoreboard()
                .map(scoreboard::getScoreboardEntries)
                .orElse(emptyList());

        return scoreboardEntries.stream()
                .filter(scoreboardEntry -> scoreboardEntry.name().getString().equals("§e" + wasteDropSpot.getDisplayName() + "§8:"))
                .map(ScoreboardEntry::value)
                .findFirst()
                .orElse(0);
    }

    private Optional<ScoreboardObjective> getGarbageManScoreboard() {
        assert MinecraftClient.getInstance().world != null; // cannot be null at this point
        Scoreboard scoreboard = MinecraftClient.getInstance().world.getScoreboard();
        ScoreboardObjective scoreboardObjective = scoreboard.getObjectiveForSlot(SIDEBAR);
        return nonNull(scoreboardObjective) && scoreboardObjective.getName().equals("müllmann") ? Optional.of(scoreboardObjective) : empty();
    }

    @Getter
    @AllArgsConstructor
    private enum WasteDropSpot {

        GLASS("Glas", new BlockPos(884, 67, 349)),
        METAL("Metall", new BlockPos(900, 67, 392)),
        WASTE("Abfall", new BlockPos(908, 67, 361)),
        WOOD("Holz", new BlockPos(876, 69, 376));

        private final String displayName;
        private final BlockPos dropSpot;
    }
}
