package de.mcmainiac.webconsole.server.commands;

import de.mcmainiac.webconsole.server.packets.ClientPacket;

public interface ExecutableCommand {
    ExecutableCommandReturnSet execute(ExecutableCommandReturnSet returnSet, ClientPacket packet);
}
