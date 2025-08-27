package de.rettichlp.pkutils.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.rettichlp.pkutils.common.registry.CommandBase;
import de.rettichlp.pkutils.common.registry.PKUtilsCommand;
import de.rettichlp.pkutils.common.storage.schema.Faction;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandSource;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

import static de.rettichlp.pkutils.PKUtilsClient.activityService;
import static de.rettichlp.pkutils.PKUtilsClient.player;
import static de.rettichlp.pkutils.PKUtilsClient.storage;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;

@PKUtilsCommand(label = "clearactivity")
public class ClearActivityCommand extends CommandBase {

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> execute(@NotNull LiteralArgumentBuilder<FabricClientCommandSource> node) {
        return node
                .then(argument("targetName", StringArgumentType.word())
                        .suggests(this::factionMemberSuggestions)
                        .executes(context -> {
                            String targetName = StringArgumentType.getString(context, "targetName");
                            activityService.clearActivity(targetName);
                            return 1;
                        })
                );
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
