package de.rettichlp.pkutils.mixin.client;

import net.minecraft.client.gui.hud.PlayerListHud;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PlayerListHud.class)
public abstract class PlayerListHudMixin {

//    @Unique
//    private static final Comparator<PlayerListEntry> PKUTILS_ENTRY_ORDERING = comparing((PlayerListEntry playerListEntry) -> ofNullable(playerListEntry.getScoreboardTeam())
//            .map(Team::getName)
//            .orElse("null"))
//            .thenComparing(playerListEntry -> playerListEntry.getProfile().getName());

//    @Inject(method = "collectPlayerEntries", at = @At("RETURN"), cancellable = true)
//    private void onCollectPlayerEntries(@NotNull CallbackInfoReturnable<List<PlayerListEntry>> cir) {
//        // get current player list entries
//        Collection<PlayerListEntry> playerListEntries = networkHandler.getListedPlayerListEntries();
//
//        // order player list entries
//        List<PlayerListEntry> orderedPlayerListEntries = playerListEntries
//                .stream()
//                .sorted(PKUTILS_ENTRY_ORDERING)
//                .limit(80L)
//                .toList();
//
//        // set ordered player list entries before the original finally returns them
//        cir.setReturnValue(orderedPlayerListEntries);
//    }

//    @Inject(method = "getPlayerComparator", at = @At("HEAD"), cancellable = true)
//    private void injectCustomComparator(CallbackInfoReturnable<Comparator<PlayerListEntry>> cir) {
//        Comparator<PlayerListEntry> customComparator = Comparator.comparing(entry -> {
//            // Beispiel: nach Spielername alphabetisch
//            return entry.getProfile().getName();
//        });
//        cir.setReturnValue(customComparator);
//    }
}
