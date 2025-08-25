package de.rettichlp.pkutils.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.rettichlp.pkutils.common.manager.CommandBase;
import de.rettichlp.pkutils.common.registry.PKUtilsCommand;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.Person;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.StringJoiner;

import static de.rettichlp.pkutils.PKUtils.MOD_ID;
import static de.rettichlp.pkutils.PKUtilsClient.player;
import static de.rettichlp.pkutils.PKUtilsClient.syncManager;
import static java.time.LocalDateTime.MIN;
import static net.minecraft.text.Text.empty;
import static net.minecraft.text.Text.of;
import static net.minecraft.util.Formatting.DARK_GRAY;
import static net.minecraft.util.Formatting.GRAY;
import static net.minecraft.util.Formatting.WHITE;

@PKUtilsCommand(label = "pkutils")
public class ModCommand extends CommandBase {

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> execute(@NotNull LiteralArgumentBuilder<FabricClientCommandSource> node) {
        return node
                .executes(context -> {
                    String version = getVersion();
                    String authors = getAuthors();
                    LocalDateTime lastSyncTimestamp = syncManager.getLastSyncTimestamp();

                    player.sendMessage(empty(), false);

                    sendModMessage("PKUtils Version " + version, false);

                    sendModMessage(empty()
                            .append(of("Autoren").copy().formatted(GRAY))
                            .append(of(":").copy().formatted(DARK_GRAY)).append(" ")
                            .append(of(authors).copy().formatted(WHITE)), false);

                    sendModMessage(empty()
                            .append(of("Discord").copy().formatted(GRAY))
                            .append(of(":").copy().formatted(DARK_GRAY)).append(" ")
                            .append(of("https://discord.gg/mZGAAwhPHu").copy().formatted(WHITE)), false);

                    sendModMessage(empty()
                            .append(of("GitHub").copy().formatted(GRAY))
                            .append(of(":").copy().formatted(DARK_GRAY)).append(" ")
                            .append(of("https://github.com/rettichlp/pkutils").copy().formatted(WHITE)), false);

                    sendModMessage(empty()
                            .append(of("Letzte Synchronisierung").copy().formatted(GRAY))
                            .append(of(":").copy().formatted(DARK_GRAY)).append(" ")
                            .append(of(lastSyncTimestamp.equals(MIN)
                                    ? "Nie"
                                    : timeToFriendlyString(lastSyncTimestamp)).copy().formatted(WHITE)), false);

                    player.sendMessage(empty(), false);

                    return 1;
                });
    }

    private String getVersion() {
        return FabricLoader.getInstance().getModContainer(MOD_ID)
                .map(modContainer -> modContainer.getMetadata().getVersion().getFriendlyString())
                .orElseThrow(() -> new NullPointerException("Cannot find version"));
    }

    private String getAuthors() {
        Collection<Person> authors = FabricLoader.getInstance().getModContainer(MOD_ID)
                .map(modContainer -> modContainer.getMetadata().getAuthors())
                .orElseThrow(() -> new NullPointerException("Cannot find authors"));

        StringJoiner stringJoiner = new StringJoiner(", ");
        authors.forEach(person -> stringJoiner.add(person.getName()));

        return stringJoiner.toString();
    }

    private String timeToFriendlyString(@NotNull ChronoLocalDateTime<LocalDate> dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        return dateTime.format(formatter);
    }
}
