package de.rettichlp.pkutils.listener;

import org.jetbrains.annotations.NotNull;

public interface ICommandSendListener {

    boolean onCommandSend(@NotNull String command);
}
