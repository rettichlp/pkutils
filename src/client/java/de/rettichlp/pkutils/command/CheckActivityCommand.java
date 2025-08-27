package de.rettichlp.pkutils.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.rettichlp.pkutils.common.manager.CommandBase;
import de.rettichlp.pkutils.common.registry.PKUtilsCommand;
import de.rettichlp.pkutils.common.storage.schema.Faction;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

import static de.rettichlp.pkutils.PKUtilsClient.player;
import static de.rettichlp.pkutils.PKUtilsClient.storage;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.minecraft.text.Text.of;

@PKUtilsCommand(label = "checkactivity")
public class CheckActivityCommand extends CommandBase {

    private static final String BASE_URL = "https://activitycheck.pkutils.eu/user/";

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> execute(@NotNull LiteralArgumentBuilder<FabricClientCommandSource> node) {
        return node
                .executes(context -> {
                    if (player == null) return 0;
                    String playerName = player.getName().getString();
                    sendActivityLink(playerName);
                    return 1;
                })
                .then(argument("playerName", StringArgumentType.word())
                        .suggests(this::factionMemberSuggestions)
                        .executes(context -> {
                            if (player == null) return 0;

                            String ownPlayerName = player.getName().getString();
                            String targetPlayerName = StringArgumentType.getString(context, "playerName");

                            if (ownPlayerName.equalsIgnoreCase(targetPlayerName)) {
                                sendActivityLink(ownPlayerName);
                                return 1;
                            }

                            Faction ownFaction = storage.getFaction(ownPlayerName);
                            Faction targetFaction = storage.getFaction(targetPlayerName);

                            if (ownFaction != Faction.NULL && ownFaction == targetFaction) {
                                sendActivityLink(targetPlayerName);
                            } else {
                                sendModMessage("§cDu kannst nur die Aktivitäten von Mitgliedern deiner eigenen Fraktion einsehen.", false);
                            }
                            return 1;
                        })
                );
    }

    private void sendActivityLink(String playerName) {
        String personalUrl = BASE_URL + playerName;
        Text linkText = of("Klicke hier, um die Aktivitäten von " + playerName + " anzuzeigen")
                .copy()
                .formatted(Formatting.GREEN)
                .styled(style -> style
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, personalUrl))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, of("Öffnet: " + personalUrl))));
        player.sendMessage(linkText, false);
    }

    private CompletableFuture<Suggestions> factionMemberSuggestions(com.mojang.brigadier.context.CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) {
        if (player == null) return Suggestions.empty();
        Faction ownFaction = storage.getFaction(player.getName().getString());
        if (ownFaction != Faction.NULL) {
            return CommandSource.suggestMatching(
                    storage.getFactionMembers(ownFaction).stream()
                            .map(factionMember -> factionMember.getPlayerName()),
                    builder
            );
        }
        return Suggestions.empty();
    }
}