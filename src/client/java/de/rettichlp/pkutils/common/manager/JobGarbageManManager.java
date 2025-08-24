package de.rettichlp.pkutils.common.manager;

import de.rettichlp.pkutils.common.listener.IMessageReceiveListener;
import de.rettichlp.pkutils.common.listener.IMoveListener;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.pkutils.PKUtilsClient.networkHandler;
import static de.rettichlp.pkutils.PKUtilsClient.player;
import static java.lang.Double.compare;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.regex.Pattern.compile;
import static net.minecraft.scoreboard.ScoreboardDisplaySlot.SIDEBAR;

public class JobGarbageManManager extends BaseManager implements IMessageReceiveListener, IMoveListener {

    private static final Pattern GARBAGE_MAN_DROP_START = compile("^\\[Müllmann] Hier kannst du auf den Haufen mit /dropwaste dein Müll sortieren!$");
    private static final Pattern GARBAGE_MAN_FINISHED = compile("^\\[Müllmann] Du hast den Job beendet\\.$");

    private final Timer timer = new Timer();
    private boolean isTimerActive = false;
    private boolean isDropStep = false;

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
    public void onMove(BlockPos blockPos) {
        if (!this.isDropStep) {
            return;
        }

        // not dropping waste currently check
        if (this.isTimerActive) {
            System.out.println("JGMM: Timer active");
            return;
        }

        // in range check
        WasteDropSpot nearestWasteDropSpot = getNearestWasteDropSpot();
        if (!player.getBlockPos().isWithinDistance(nearestWasteDropSpot.getDropSpot(), 3)) {
            System.out.println("JGMM: Not in range");
            return;
        }

        // start drop timer
        this.isTimerActive = true;
        this.timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (getWasteLeft(nearestWasteDropSpot) <= 0) {
                    System.out.println("JGMM: No waste left for " + nearestWasteDropSpot.getDisplayName());
                    cancel();
                    JobGarbageManManager.this.isTimerActive = false;
                    return;
                }

                networkHandler.sendChatCommand("dropwaste");
            }
        }, 0, 5000);
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
                .filter(scoreboardEntry -> scoreboardEntry.name().getString().equals(wasteDropSpot.getDisplayName() + ":"))
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
