package de.rettichlp.mixin.client;

import de.rettichlp.common.storage.schema.Faction;
import de.rettichlp.common.storage.schema.WantedEntry;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Optional;

import static de.rettichlp.PKUtilsClient.storage;
import static java.util.Objects.nonNull;
import static net.minecraft.text.Text.empty;
import static net.minecraft.util.Formatting.DARK_GREEN;
import static net.minecraft.util.Formatting.DARK_RED;
import static net.minecraft.util.Formatting.GOLD;
import static net.minecraft.util.Formatting.GREEN;
import static net.minecraft.util.Formatting.RED;
import static net.minecraft.util.Formatting.YELLOW;

@Mixin(EntityRenderer.class)
public abstract class NametagColorMixin<S extends Entity, T extends EntityRenderState> {

    @ModifyVariable(
            method = "renderLabelIfPresent(Lnet/minecraft/client/render/entity/state/EntityRenderState;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private Text changeNameColor(Text original, EntityRenderState state) {
        if (state instanceof PlayerEntityRenderState playerState && nonNull(playerState.displayName)) {
            Text displayName = playerState.displayName;
            String displayNameString = displayName.getString();

            Optional<WantedEntry> optionalWantedEntry = storage.getWantedEntries().stream()
                    .filter(wantedEntry -> wantedEntry.getPlayerName().equals(displayNameString))
                    .findAny();

            Faction faction = storage.getFaction(displayNameString);
            Text nameTagSuffix = faction.getNameTagSuffix();

            return optionalWantedEntry.isEmpty()
                    ? empty()
                    .append(displayName.copy())
                    .append(" ")
                    .append(nameTagSuffix)
                    : empty()
                    .append(displayName.copy().styled(style -> style.withColor(getWantedPointColor(optionalWantedEntry.get()))))
                    .append(" ")
                    .append(nameTagSuffix);
        }

        return original;
    }

    @Unique
    @Contract(pure = true)
    private @NotNull Formatting getWantedPointColor(@NotNull WantedEntry wantedEntry) {
        int wantedPointAmount = wantedEntry.getWantedPointAmount();
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
