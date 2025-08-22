package de.rettichlp.pkutils.common.manager;

import de.rettichlp.pkutils.common.listener.IMessageListener;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.pkutils.PKUtilsClient.networkHandler;
import static de.rettichlp.pkutils.PKUtilsClient.player;
import static java.lang.Double.compare;
import static java.util.Arrays.stream;
import static java.util.regex.Pattern.compile;

@NoArgsConstructor
public class JobFisherManager extends BaseManager implements IMessageListener {

    private static final Pattern FISHER_START = compile("^\\[Fischer] Mit /findschwarm kannst du dir den nächsten Fischschwarm anzeigen lassen\\.$");
    private static final Pattern FISHER_SPOT_FOUND_PATTERN = compile("^\\[Fischer] Du hast einen Fischschwarm gefunden!$");
    private static final Pattern FISHER_CATCH_SUCCESS = compile("^\\[Fischer] Du hast \\d+kg frischen Fisch gefangen! \\(\\d+kg\\)$");
    private static final Pattern FISHER_CATCH_FAILURE = compile("^\\[Fischer] Du hast das Fischernetz verloren\\.\\.\\.$");

    private Collection<FisherJobSpot> currentFisherJobSpots = new ArrayList<>();

    @Override
    public boolean onMessage(String message) {
        Matcher fisherStartMatcher = FISHER_START.matcher(message);
        if (fisherStartMatcher.find()) {
            this.currentFisherJobSpots = new ArrayList<>();
            String naviCommand = FisherJobSpot.SPOT_1.getNaviCommand();
            networkHandler.sendChatCommand(naviCommand);
            return true;
        }

        Matcher fisherSpotFoundMatcher = FISHER_SPOT_FOUND_PATTERN.matcher(message);
        if (fisherSpotFoundMatcher.find()) {
            networkHandler.sendChatCommand("catchfish");
            FisherJobSpot nearestFisherJobSpot = getNearestFisherJobSpot(getNotVisitedFisherJobSpots()).orElseThrow();
            this.currentFisherJobSpots.add(nearestFisherJobSpot);
            delayedAction(() -> networkHandler.sendChatCommand("stoproute"), 1000);
            return true;
        }

        Matcher fisherCatchSuccessMatcher = FISHER_CATCH_SUCCESS.matcher(message);
        Matcher fisherCatchFailureMatcher = FISHER_CATCH_FAILURE.matcher(message);
        if (fisherCatchSuccessMatcher.find() || fisherCatchFailureMatcher.find()) {
            // get nearest
            Optional<FisherJobSpot> nearestFisherJobSpot = getNearestFisherJobSpot(getNotVisitedFisherJobSpots());

            nearestFisherJobSpot.ifPresentOrElse(fisherJobSpot -> {
                String naviCommand = fisherJobSpot.getNaviCommand();
                networkHandler.sendChatCommand(naviCommand);
            }, () -> {
                // all spots visited, go to harbor
                networkHandler.sendChatCommand("navi -504 63 197");
            });

            return true;
        }

        if (message.equals("Du hast dein Ziel erreicht!") && this.currentFisherJobSpots.size() == 5) {
            this.currentFisherJobSpots = new ArrayList<>();
            networkHandler.sendChatCommand("dropfish");
        }

        return true;
    }

    private @NotNull Optional<FisherJobSpot> getNearestFisherJobSpot(@NotNull Collection<FisherJobSpot> fisherJobSpots) {
        return fisherJobSpots.stream()
                .min((spot1, spot2) -> {
                    double distance1 = player.squaredDistanceTo(spot1.getPosition().getX(), spot1.getPosition().getY(), spot1.getPosition().getZ());
                    double distance2 = player.squaredDistanceTo(spot2.getPosition().getX(), spot2.getPosition().getY(), spot2.getPosition().getZ());
                    return compare(distance1, distance2);
                });
    }

    private @NotNull @Unmodifiable List<FisherJobSpot> getNotVisitedFisherJobSpots() {
        return stream(FisherJobSpot.values())
                .filter(fisherJobSpot -> !this.currentFisherJobSpots.contains(fisherJobSpot))
                .toList();
    }

    @Getter
    @AllArgsConstructor
    private enum FisherJobSpot {

        SPOT_1(new BlockPos(-568, 63, 158)),
        SPOT_2(new BlockPos(-547, 63, 104)),
        SPOT_3(new BlockPos(-562, 63, 50)),
        SPOT_4(new BlockPos(-506, 63, 18)),
        SPOT_5(new BlockPos(-452, 63, 26));

        private final BlockPos position;

        public @NotNull String getNaviCommand() {
            return "navi " + this.position.getX() + " " + this.position.getY() + " " + this.position.getZ();
        }
    }
}
