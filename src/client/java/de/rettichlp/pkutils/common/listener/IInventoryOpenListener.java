package de.rettichlp.pkutils.common.listener;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.screen.ScreenHandler;

public interface IInventoryOpenListener {

    void onInventoryOpen(ScreenHandler screenHandler, ClientPlayerInteractionManager interactionManager);
}
