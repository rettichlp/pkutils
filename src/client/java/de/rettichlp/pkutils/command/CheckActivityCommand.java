package de.rettichlp.pkutils.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.rettichlp.pkutils.common.registry.CommandBase;
import de.rettichlp.pkutils.common.registry.PKUtilsCommand;
import de.rettichlp.pkutils.common.storage.schema.Faction;
import de.rettichlp.pkutils.common.storage.schema.FactionMember;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static com.mojang.brigadier.suggestion.Suggestions.*;
import static de.rettichlp.pkutils.PKUtilsClient.player;
import static de.rettichlp.pkutils.PKUtilsClient.storage;
import static de.rettichlp.pkutils.common.storage.schema.Faction.NULL;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.minecraft.command.CommandSource.suggestMatching;
import static net.minecraft.text.ClickEvent.Action.*;
import static net.minecraft.text.HoverEvent.Action.SHOW_TEXT;
import static net.minecraft.text.Text.of;

@PKUtilsCommand(label = "checkactivity")
public class CheckActivityCommand extends CommandBase {

    private static final String BASE_URL = "https://activitycheck.pkutils.eu";

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> execute(@NotNull LiteralArgumentBuilder<FabricClientCommandSource> node) {
        return node
                .then(argument("player", word())
                        .suggests((context, builder) -> {
                            Faction faction = storage.getFaction(player.getName().getString());
                            return faction == NULL ? empty() : suggestMatching(faction.getMembers().stream()
                                    .map(FactionMember::getPlayerName), builder);
                        })
                        .executes(context -> {
                            String playerName = player.getName().getString();
                            Faction faction = storage.getFaction(playerName);

                            String targetName = getString(context, "player");
                            Faction targetFaction = storage.getFaction(targetName);

                            boolean isNotSuperUser = !"25855f4d-3874-4a7f-a6ad-e9e4f3042e19".equals(player.getUuidAsString());

                            if (isNotSuperUser && faction != targetFaction) {
                                sendModMessage("Der Spieler ist nicht in deiner Fraktion.", false);
                                return 1;
                            }

                            if (isNotSuperUser && targetFaction == NULL) {
                                sendModMessage("Der Spieler ist in keiner Fraktion.", false);
                                return 1;
                            }

                            if (isNotSuperUser && storage.getFactionMembers(faction).stream()
                                    .filter(factionMember -> factionMember.getPlayerName().equals(playerName))
                                    .findFirst()
                                    .map(factionMember -> factionMember.getRank() < 4)
                                    .orElse(true)) {
                                sendModMessage("Du musst Rang 4 oder höher sein, um die Aktivitäten von anderen Mitgliedern einsehen zu können.", false);
                                return 1;
                            }

                            sendActivityLink(targetName);
                            return 1;
                        })
                )
                .executes(context -> {
                    String playerName = player.getName().getString();
                    sendActivityLink(playerName);
                    return 1;
                });
    }

    private void sendActivityLink(String playerName) {
        String personalUrl = BASE_URL + "/user/" + playerName;

        MutableText linkText = modMessagePrefix.copy()
                .append(of("Klicke hier, um die Aktivitäten von")).append(" ")
                .append(of(playerName).copy().formatted(Formatting.BLUE, Formatting.UNDERLINE)).append(" ")
                .append(of("anzuzeigen."));

        MutableText clickableLinkText = linkText.styled(style -> {
            style.withClickEvent(new ClickEvent(OPEN_URL, personalUrl));
            style.withHoverEvent(new HoverEvent(SHOW_TEXT, of("Öffnet: " + personalUrl)));
            return style;
        });

        player.sendMessage(clickableLinkText, false);
    }
}
