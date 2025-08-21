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

    NULL("", "Keine Auswahl", false, GRAY, ""),
    FBI("fbi", "F.B.I.", false, DARK_BLUE, "✯"),
    POLIZEI("polizei", "Polizei", false, BLUE, "✯"),
    RETTUNGSDIENST("rettungsdienst", "Rettungsdienst", false, DARK_RED, "✚"),

    CALDERON("calderon", "Calderón Kartell", true, GOLD, "☀"),
    KERZAKOV("kerzakov", "Kerzakov Familie", true, RED, "✮"),
    LACOSANOSTRA("lacosanostra", "La Cosa Nostra", true, DARK_AQUA, "⚜"),
    LEMILIEU("le_milieu", "Le Milieu", true, DARK_AQUA, "Ⓜ"),
    // OBRIEN("obrien", "O'brien Familie", true, DARK_GREEN, "☘"),
    WESTSIDEBALLAS("westsideballas", "Westside Ballas", true, DARK_PURPLE, "☠"),

    HITMAN("hitman", "Hitman", false, AQUA, "➹"),
    KIRCHE("kirche", "Kirche", false, LIGHT_PURPLE, "†"),
    NEWS("news", "News", false, YELLOW, "✉"),
    TERRORISTEN("terroristen", "Terroristen", false, GRAY, "❇");

    private final String apiName;
    private final String displayName;
    private final boolean isBadFaction;
    private final Formatting color;
    private final String icon;

    public static @NotNull Optional<Faction> fromDisplayName(String displayName) {
        return stream(Faction.values())
                .filter(faction -> displayName.contains(faction.getDisplayName()))
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
