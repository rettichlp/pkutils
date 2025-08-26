package de.rettichlp.pkutils.common.services;

import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import static net.minecraft.util.Formatting.DARK_GREEN;
import static net.minecraft.util.Formatting.DARK_RED;
import static net.minecraft.util.Formatting.GOLD;
import static net.minecraft.util.Formatting.GREEN;
import static net.minecraft.util.Formatting.RED;
import static net.minecraft.util.Formatting.YELLOW;

@Getter
@Setter
public class FactionService extends PKUtilsBase {

    public @NotNull Formatting getWantedPointColor(int wantedPointAmount) {
        Formatting color;

        if (wantedPointAmount >= 60) {
            color = DARK_RED;
        } else if (wantedPointAmount >= 50) {
            color = RED;
        } else if (wantedPointAmount >= 25) {
            color = GOLD;
        } else if (wantedPointAmount >= 15) {
            color = YELLOW;
        } else if (wantedPointAmount >= 2) {
            color = GREEN;
        } else {
            color = DARK_GREEN;
        }
        return color;
    }
}
