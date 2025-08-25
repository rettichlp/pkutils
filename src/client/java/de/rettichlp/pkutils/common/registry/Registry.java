package de.rettichlp.pkutils.common.registry;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.rettichlp.pkutils.common.manager.CommandBase;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import org.atteo.classindex.ClassIndex;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;

import static com.mojang.text2speech.Narrator.LOGGER;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class Registry {

    public static void registerCommands(@NotNull CommandDispatcher<FabricClientCommandSource> dispatcher) {
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
}
