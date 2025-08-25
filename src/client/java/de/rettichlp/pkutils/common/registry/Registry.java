package de.rettichlp.pkutils.common.registry;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.rettichlp.pkutils.common.listener.ICommandSendListener;
import de.rettichlp.pkutils.common.listener.IMessageReceiveListener;
import de.rettichlp.pkutils.common.listener.IMessageSendListener;
import de.rettichlp.pkutils.common.listener.IMoveListener;
import de.rettichlp.pkutils.common.listener.INaviSpotReachedListener;
import de.rettichlp.pkutils.common.manager.CommandBase;
import de.rettichlp.pkutils.common.manager.PKUtilsBase;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.util.math.BlockPos;
import org.atteo.classindex.ClassIndex;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;

import static com.mojang.text2speech.Narrator.LOGGER;
import static de.rettichlp.pkutils.PKUtilsClient.networkHandler;
import static de.rettichlp.pkutils.PKUtilsClient.player;
import static java.util.Objects.isNull;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class Registry {

    private BlockPos lastPlayerPos = null;

    public void registerCommands(@NotNull CommandDispatcher<FabricClientCommandSource> dispatcher) {
        for (Class<?> commandClass : ClassIndex.getAnnotated(PKUtilsCommand.class)) {
            try {
                String label = commandClass.getAnnotation(PKUtilsCommand.class).label();
                CommandBase commandInstance = (CommandBase) commandClass.getConstructor().newInstance();

                LiteralArgumentBuilder<FabricClientCommandSource> node = literal(label);
                LiteralArgumentBuilder<FabricClientCommandSource> enrichedNode = commandInstance.execute(node);

                dispatcher.register(enrichedNode);
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                LOGGER.error("Error while registering command: {}", commandClass.getName(), e.getCause());
            }
        }
    }

    public void registerListeners() {
        // ignore messages until the player is initialized
        if (player == null || networkHandler == null) {
            return;
        }

        for (Class<?> listenerClass : ClassIndex.getAnnotated(PKUtilsListener.class)) {
            try {
                PKUtilsBase listenerInstance = (PKUtilsBase) listenerClass.getConstructor().newInstance();

                if (listenerInstance instanceof ICommandSendListener iCommandSendListener) {
                    ClientSendMessageEvents.ALLOW_COMMAND.register(iCommandSendListener::onCommandSend);
                }

                if (listenerInstance instanceof IMessageReceiveListener iMessageReceiveListener) {
                    ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
                        String rawMessage = message.getString();
                        return iMessageReceiveListener.onMessageReceive(rawMessage);
                    });
                }

                if (listenerInstance instanceof IMessageSendListener iMessageSendListener) {
                    ClientSendMessageEvents.ALLOW_CHAT.register(iMessageSendListener::onMessageSend);
                }

                if (listenerInstance instanceof IMoveListener iMoveListener) {
                    ClientTickEvents.END_CLIENT_TICK.register((server) -> {
                        BlockPos blockPos = player.getBlockPos();
                        if (isNull(this.lastPlayerPos) || !this.lastPlayerPos.equals(blockPos)) {
                            this.lastPlayerPos = blockPos;
                            iMoveListener.onMove(blockPos);
                        }
                    });
                }

                if (listenerInstance instanceof INaviSpotReachedListener iNaviSpotReachedListener) {
                    ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
                        String rawMessage = message.getString();
                        if (rawMessage.equals("Du hast dein Ziel erreicht!")) {
                            iNaviSpotReachedListener.onNaviSpotReached();
                        }

                        return true;
                    });
                }
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                LOGGER.error("Error while registering listener: {}", listenerClass.getName(), e.getCause());
            }
        }
    }
}
