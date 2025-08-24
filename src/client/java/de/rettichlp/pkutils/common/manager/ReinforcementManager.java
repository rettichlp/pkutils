package de.rettichlp.pkutils.common.manager;

import de.rettichlp.pkutils.common.listener.ICommandSendListener;
import de.rettichlp.pkutils.common.listener.IMessageReceiveListener;
import de.rettichlp.pkutils.common.storage.schema.Faction;
import de.rettichlp.pkutils.common.storage.schema.FactionMember;
import de.rettichlp.pkutils.common.storage.schema.Reinforcement;
import de.rettichlp.pkutils.common.storage.schema.WantedEntry;
import lombok.NoArgsConstructor;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static de.rettichlp.pkutils.PKUtilsClient.player;
import static de.rettichlp.pkutils.PKUtilsClient.storage;
import static de.rettichlp.pkutils.PKUtilsClient.syncManager;
import static de.rettichlp.pkutils.common.storage.schema.Reinforcement.Type.fromArgument;
import static java.lang.Integer.parseInt;
import static java.lang.String.valueOf;
import static java.lang.System.currentTimeMillis;
import static java.util.Objects.requireNonNull;
import static java.util.regex.Pattern.compile;
import static net.minecraft.text.Text.empty;
import static net.minecraft.text.Text.of;
import static net.minecraft.util.Formatting.BLUE;
import static net.minecraft.util.Formatting.DARK_AQUA;
import static net.minecraft.util.Formatting.DARK_GRAY;
import static net.minecraft.util.Formatting.DARK_GREEN;
import static net.minecraft.util.Formatting.DARK_RED;
import static net.minecraft.util.Formatting.GOLD;
import static net.minecraft.util.Formatting.GRAY;
import static net.minecraft.util.Formatting.GREEN;
import static net.minecraft.util.Formatting.RED;
import static net.minecraft.util.Formatting.YELLOW;

@NoArgsConstructor
public class ReinforcementManager extends BaseManager implements ICommandSendListener, IMessageReceiveListener {

    private static final Pattern REINFORCEMENT_SEND_PATTERN = compile("^/reinf(orcement)?(?<argument> -\\w+)?$");
    private static final Pattern REINFORCEMENT_PATTERN = compile("^$");
    private static final Pattern REINFORCEMENT_ACCEPT_PATTERN = compile("^$");

    private final Set<Reinforcement> reinforcements = new HashSet<>();

    @Override
    public boolean onCommandSend(String command) {
        Matcher reinforcementSendMatcher = REINFORCEMENT_SEND_PATTERN.matcher(command);
        if (reinforcementSendMatcher.find()) {
            fromArgument(reinforcementSendMatcher.group("argument").trim()).ifPresent(type -> {
                Reinforcement reinforcement = new Reinforcement(type, requireNonNull(player.getDisplayName()).getString());
                this.reinforcements.add(reinforcement);
            });
        }

        return false;
    }

    @Override
    public boolean onMessageReceive(String message) {
//        Matcher reinforcementAcceptMatcher = REINFORCEMENT_ACCEPT_PATTERN.matcher(message);
//        if (reinforcementAcceptMatcher.find()) {
//            String executingPlayerName = reinforcementAcceptMatcher.group("executingPlayerName");
//            String comingPlayerName = reinforcementAcceptMatcher.group("comingPlayerName");
//
//            storage.getWantedEntries().stream()
//                    .filter(wantedEntry -> wantedEntry.getPlayerName().equals(playerName))
//                    .findFirst()
//                    .ifPresent(wantedEntry -> wantedEntry.setReason(reason));
//
//            Text modifiedMessage = empty()
//                    .append(of("Gesucht").copy().formatted(RED)).append(" ")
//                    .append(of("-").copy().formatted(GRAY)).append(" ")
//                    .append(of(wantedGivenReasonMatcher.group(1)).copy().formatted(BLUE)).append(" ")
//                    .append(of("-").copy().formatted(GRAY)).append(" ")
//                    .append(of(wantedGivenReasonMatcher.group(2)).copy().formatted(BLUE));
//
//            player.sendMessage(modifiedMessage, false);
//
//            return false;
//        }

        return true;
    }
}
