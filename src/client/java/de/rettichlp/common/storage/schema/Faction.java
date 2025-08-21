package de.rettichlp.common.storage.schema;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static java.util.Arrays.stream;
import static net.minecraft.text.Text.empty;
import static net.minecraft.util.Formatting.AQUA;
import static net.minecraft.util.Formatting.BLUE;
import static net.minecraft.util.Formatting.DARK_AQUA;
import static net.minecraft.util.Formatting.DARK_BLUE;
import static net.minecraft.util.Formatting.DARK_GRAY;
import static net.minecraft.util.Formatting.DARK_GREEN;
import static net.minecraft.util.Formatting.DARK_PURPLE;
import static net.minecraft.util.Formatting.DARK_RED;
import static net.minecraft.util.Formatting.GOLD;
import static net.minecraft.util.Formatting.GRAY;
import static net.minecraft.util.Formatting.LIGHT_PURPLE;
import static net.minecraft.util.Formatting.RED;
import static net.minecraft.util.Formatting.YELLOW;

@Getter
@AllArgsConstructor
public enum Faction {

    NULL("", "Keine Auswahl", "", false, GRAY, ""),
    FBI("fbi", "FBI", "FBI", false, DARK_BLUE, "✯"),
    POLIZEI("polizei", "Polizei", "Polizei", false, BLUE, "✯"),
    RETTUNGSDIENST("rettungsdienst", "Rettungsdienst", "Rettungsdienst", false, DARK_RED, "✚"),

    CALDERON("calderon", "Calderón Kartell", "Kartell", true, GOLD, "☀"),
    KERZAKOV("kerzakov", "Kerzakov Familie", "Kerzakov", true, RED, "✮"),
    LACOSANOSTRA("lacosanostra", "La Cosa Nostra", "Mafia", true, DARK_AQUA, "⚜"),
    LEMILIEU("le_milieu", "Le Milieu", "France", true, DARK_AQUA, "Ⓜ"),
    OBRIEN("obrien", "O'brien Familie", "Obrien", true, DARK_GREEN, "☘"),
    WESTSIDEBALLAS("westsideballas", "Westside Ballas", "Gang", true, DARK_PURPLE, "☠"),

    HITMAN("hitman", "Hitman", "Hitman", false, AQUA, "➹"),
    KIRCHE("kirche", "Kirche", "Kirche", false, LIGHT_PURPLE, "†"),
    NEWS("news", "News Agency", "News", false, YELLOW, "✉"),
    TERRORISTEN("terroristen", "Terroristen", "Terroristen", false, GRAY, "❇");

    private final String apiName;
    private final String displayName;
    private final String factionKey;
    private final boolean isBadFaction;
    private final Formatting color;
    private final String icon;

    public static @NotNull Optional<Faction> fromDisplayName(String displayName) {
        return stream(Faction.values())
                .filter(faction -> faction.getDisplayName()
                        .equalsIgnoreCase(displayName))
                .findFirst();
    }

    public static @NotNull Optional<Faction> fromFactionKey(String factionKey) {
        return stream(Faction.values())
                .filter(faction -> faction.getFactionKey()
                        .equalsIgnoreCase(factionKey))
                .findFirst();
    }

    public Text getNameTagSuffix() {
        return this == NULL ? empty() : empty()
                .append(Text.of("◤")
                        .copy()
                        .formatted(DARK_GRAY))
                .append(Text.of(this.icon)
                        .copy()
                        .formatted(this.color))
                .append(Text.of("◢")
                        .copy()
                        .formatted(DARK_GRAY));
    }
}
