package de.rettichlp.common.manager;

import lombok.NoArgsConstructor;

import java.util.Timer;
import java.util.TimerTask;

@NoArgsConstructor
public class BaseManager {

    public void delayedAction(Runnable runnable, long milliseconds) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runnable.run();
            }
        }, milliseconds);
    }
}
