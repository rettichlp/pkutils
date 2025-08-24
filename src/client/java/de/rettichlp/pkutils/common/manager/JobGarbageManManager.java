package de.rettichlp.pkutils.common.manager;

import de.rettichlp.pkutils.common.listener.IPileOfWasteReached;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import static de.rettichlp.pkutils.PKUtilsClient.player;
import static de.rettichlp.pkutils.PKUtilsClient.networkHandler;

public class JobGarbageManManager implements IPileOfWasteReached {

    private final BlockPos[] garbageBinLocations = {
            new BlockPos(876, 69, 376), // Holz
            new BlockPos(884, 67, 349), // Glas
            new BlockPos(900, 67, 392), // Metall
            new BlockPos(908, 67, 361)  // Abfall
    };

    @Override
    public void reachedPileOfWaste() {
        BlockPos playerPos = player.getBlockPos();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {

                Arrays.stream(garbageBinLocations)
                        .filter(location -> isNear(playerPos, location, 5))
                        .findFirst()
                        .ifPresentOrElse(location -> {
                            networkHandler.sendChatCommand("dropwaste");
                        }, () -> {

                        });
            }
        }, 0, 6000);
    }

    private boolean isNear(BlockPos playerPos, BlockPos targetPos, int radius) {
        return playerPos.isWithinDistance(targetPos, radius);
    }
}