package de.rettichlp.pkutils.common.manager;

import net.minecraft.text.Text;

import java.util.Timer;
import java.util.TimerTask;

import static de.rettichlp.pkutils.PKUtilsClient.player;
import static net.minecraft.client.MinecraftClient.getInstance;
import static net.minecraft.text.Text.of;
import static net.minecraft.util.Formatting.DARK_GRAY;
import static net.minecraft.util.Formatting.DARK_PURPLE;
import static net.minecraft.util.Formatting.LIGHT_PURPLE;
import static net.minecraft.util.Formatting.WHITE;

public abstract class ManagerBase {

    private static final Text modMessagePrefix = Text.empty()
            .append(of("✦").copy().formatted(DARK_PURPLE))
            .append(of(" "))
            .append(of("PKU").copy().formatted(LIGHT_PURPLE))
            .append(of(" "))
            .append(of("|").copy().formatted(DARK_GRAY))
            .append(of(" "));

    public void sendModMessage(String message, boolean inActionbar) {
        sendModMessage(of(message).copy().formatted(WHITE), inActionbar);
    }

    public void sendModMessage(Text message, boolean inActionbar) {
        Text messageText = modMessagePrefix.copy().append(message);
        player.sendMessage(messageText, inActionbar);
    }

    public void delayedAction(Runnable runnable, long milliseconds) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                getInstance().execute(runnable);
            }
        }, milliseconds);
    }
}
