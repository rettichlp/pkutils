package de.rettichlp.pkutils.mixin.client;

import de.rettichlp.pkutils.common.storage.schema.BlacklistEntry;
import de.rettichlp.pkutils.common.storage.schema.Faction;
import de.rettichlp.pkutils.common.storage.schema.WantedEntry;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Optional;

import static de.rettichlp.pkutils.PKUtilsClient.storage;
import static de.rettichlp.pkutils.PKUtilsClient.wantedManager;
import static java.util.Objects.nonNull;
import static net.minecraft.text.Text.empty;
import static net.minecraft.text.Text.of;
import static net.minecraft.util.Formatting.DARK_GRAY;
import static net.minecraft.util.Formatting.DARK_RED;
import static net.minecraft.util.Formatting.RED;
import static net.minecraft.util.Formatting.WHITE;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<S extends Entity, T extends EntityRenderState> {

    @ModifyVariable(
            method = "renderLabelIfPresent(Lnet/minecraft/client/render/entity/state/EntityRenderState;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private Text renderLabelIfPresent(Text original, EntityRenderState state) {
        if (state instanceof PlayerEntityRenderState playerState && nonNull(playerState.displayName)) {
            Text targetDisplayName = playerState.displayName;
            String targetDisplayNameString = targetDisplayName.getString();
            Faction targetFaction = storage.getFaction(targetDisplayNameString);

            Text newTargetDisplayNamePrefix = empty();
            Text newTargetDisplayName = targetDisplayName.copy();
            Text newTargetDisplayNameSuffix = targetFaction.getNameTagSuffix();
            Formatting newTargetDisplayNameColor = WHITE;

            Optional<BlacklistEntry> optionalTargetBlacklistEntry = storage.getBlacklistEntries().stream()
                    .filter(blacklistEntry -> blacklistEntry.getPlayerName().equals(targetDisplayNameString))
                    .findAny();

            if (optionalTargetBlacklistEntry.isPresent()) {
                newTargetDisplayNameColor = RED;
                newTargetDisplayNamePrefix = !optionalTargetBlacklistEntry.get().isOutlaw() ? empty() : empty()
                        .append(of("[").copy().formatted(DARK_GRAY))
                        .append(of("V").copy().formatted(DARK_RED))
                        .append(of("]").copy().formatted(DARK_GRAY));
            }

            Optional<WantedEntry> optionalTargetWantedEntry = storage.getWantedEntries().stream()
                    .filter(wantedEntry -> wantedEntry.getPlayerName().equals(targetDisplayNameString))
                    .findAny();

            if (optionalTargetWantedEntry.isPresent()) {
                newTargetDisplayNameColor = wantedManager.getWantedPointColor(optionalTargetWantedEntry.get().getWantedPointAmount());
            }

            return empty()
                    .append(newTargetDisplayNamePrefix)
                    .append(" ")
                    .append(newTargetDisplayName.copy().formatted(newTargetDisplayNameColor))
                    .append(" ")
                    .append(newTargetDisplayNameSuffix);
        }

        return original;
    }
}
