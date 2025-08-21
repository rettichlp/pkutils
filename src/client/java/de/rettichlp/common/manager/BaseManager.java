package de.rettichlp.common.manager;

import lombok.NoArgsConstructor;

import java.util.Timer;
import java.util.TimerTask;

import static net.minecraft.client.MinecraftClient.getInstance;

@NoArgsConstructor
public class BaseManager {

    public void delayedAction(Runnable runnable, long milliseconds) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                getInstance().execute(runnable);
            }
        }, milliseconds);
    }
}
