package de.rettichlp.pkutils.listener.impl.faction;

import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import de.rettichlp.pkutils.common.registry.PKUtilsListener;
import de.rettichlp.pkutils.common.storage.Storage;
import de.rettichlp.pkutils.listener.IMessageReceiveListener;
import de.rettichlp.pkutils.listener.IMessageSendListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.pkutils.PKUtilsClient.networkHandler;
import static de.rettichlp.pkutils.PKUtilsClient.player;
import static de.rettichlp.pkutils.PKUtilsClient.storage;
import static de.rettichlp.pkutils.common.storage.Storage.ToggledChat.NONE;
import static java.util.Optional.ofNullable;
import static java.util.regex.Pattern.compile;
import static net.minecraft.text.Text.empty;
import static net.minecraft.text.Text.of;
import static net.minecraft.util.Formatting.AQUA;
import static net.minecraft.util.Formatting.BOLD;
import static net.minecraft.util.Formatting.DARK_AQUA;
import static net.minecraft.util.Formatting.GRAY;
import static net.minecraft.util.Formatting.RED;

@PKUtilsListener
public class FactionListener extends PKUtilsBase implements IMessageReceiveListener, IMessageSendListener {

    private static final Pattern REINFORCEMENT_PATTERN = compile("^(?:(?<type>.+)! )?(?<sender>.+) benötigt Unterstützung in der Nähe von (?<naviPoint>.+) \\((?<distance>\\d+)m\\)!$");
    private static final Pattern REINFORCEMENT_BUTTON_PATTERN = compile("^ §7» §cRoute anzeigen §7\\| §cUnterwegs$");
    private static final Pattern REINFORCMENT_ON_THE_WAY_PATTERN = compile("^(?<sender>.+) kommt zum Verstärkungsruf von (?<target>.+)! \\((?<distance>\\d+) Meter entfernt\\)$");

    private static final ReinforcementConsumer<String, String, String, String> REINFORCEMENT = (type, sender, naviPoint, distance) -> empty()
            .append(of(type).copy().formatted(RED, BOLD)).append(" ")
            .append(of(sender).copy().formatted(AQUA)).append(" ")
            .append(of("-").copy().formatted(GRAY)).append(" ")
            .append(of(naviPoint).copy().formatted(AQUA)).append(" ")
            .append(of("-").copy().formatted(GRAY)).append(" ")
            .append(of(distance + "m").copy().formatted(DARK_AQUA));

    private static final ReinforcementOnTheWayConsumer<String, String, String> REINFORCEMENT_ON_THE_WAY = (sender, target, distance) -> empty()
            .append(of("➥").copy().formatted(GRAY)).append(" ")
            .append(of(sender).copy().formatted(AQUA)).append(" ")
            .append(of("➡").copy().formatted(GRAY)).append(" ")
            .append(of(target).copy().formatted(DARK_AQUA)).append(" ")
            .append(of("- (").copy().formatted(GRAY))
            .append(of(distance + "m").copy().formatted(DARK_AQUA))
            .append(of(")").copy().formatted(GRAY));

    @Override
    public boolean onMessageReceive(String message) {
        Matcher reinforcementMatcher = REINFORCEMENT_PATTERN.matcher(message);
        if (reinforcementMatcher.find()) {
            String type = ofNullable(reinforcementMatcher.group("type")).orElse("Reinforcement");
            String sender = reinforcementMatcher.group("sender");
            String naviPoint = reinforcementMatcher.group("naviPoint");
            String distance = reinforcementMatcher.group("distance");

            Text reinforcement = REINFORCEMENT.create(type, sender, naviPoint, distance);
            player.sendMessage(empty(), false);
            player.sendMessage(reinforcement, false);

            return false;
        }

        Matcher reinforcementButtonMatcher = REINFORCEMENT_BUTTON_PATTERN.matcher(message);
        if (reinforcementButtonMatcher.find()) {
            // send empty line after buttons
            MinecraftClient.getInstance().execute(() -> player.sendMessage(empty(), false));
            return true;
        }

        Matcher reinforcementOnTheWayMatcher = REINFORCMENT_ON_THE_WAY_PATTERN.matcher(message);
        if (reinforcementOnTheWayMatcher.find()) {
            String sender = reinforcementOnTheWayMatcher.group("sender");
            String target = reinforcementOnTheWayMatcher.group("target");
            String distance = reinforcementOnTheWayMatcher.group("distance");

            Text reinforcementAnswer = REINFORCEMENT_ON_THE_WAY.create(sender, target, distance);
            player.sendMessage(reinforcementAnswer, false);

            return false;
        }

        return true;
    }

    @Override
    public boolean onMessageSend(String message) {
        Storage.ToggledChat toggledChat = storage.getToggledChat();
        if (toggledChat != NONE) {
            networkHandler.sendChatCommand(toggledChat.getCommand() + " " + message);
            return false;
        }

        return true;
    }

    @FunctionalInterface
    public interface ReinforcementConsumer<Type, Sender, NaviPoint, Distance> {

        Text create(String type, String sender, String naviPoint, String distance);
    }

    @FunctionalInterface
    public interface ReinforcementOnTheWayConsumer<Sender, Target, Distance> {

        Text create(String sender, String target, String distance);
    }
}
