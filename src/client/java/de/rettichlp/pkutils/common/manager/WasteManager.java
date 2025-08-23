package de.rettichlp.pkutils.common.manager;

import de.rettichlp.pkutils.common.listener.IInventoryOpenListener;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.screen.HopperScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.collection.DefaultedList;

import static de.rettichlp.pkutils.PKUtilsClient.player;
import static net.minecraft.screen.slot.SlotActionType.PICKUP;

public class WasteManager extends BaseManager implements IInventoryOpenListener {

    @Override
    public void onInventoryOpen(ScreenHandler screenHandler, ClientPlayerInteractionManager interactionManager) {
        if (screenHandler instanceof HopperScreenHandler hopperScreenHandler) {
            DefaultedList<Slot> slots = hopperScreenHandler.slots;

            slots.stream().filter(Slot::hasStack).findFirst().ifPresent(slot -> interactionManager.clickSlot(
                    hopperScreenHandler.syncId,
                    slot.id,
                    0, // 0 = left click, 1 = right click, 2 = middle click, 4 = drop, 6 = double click
                    PICKUP,
                    player
            ));
        }
    }
}
