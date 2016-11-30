package de.mcmainiac.webconsole.server.commands.impl;

import de.mcmainiac.webconsole.server.commands.ExecutableCommand;
import de.mcmainiac.webconsole.server.commands.ExecutableCommandReturnSet;
import de.mcmainiac.webconsole.server.commands.ServerResponse;
import de.mcmainiac.webconsole.server.packets.ClientPacket;

public class Ping implements ExecutableCommand {
    @Override
    public ExecutableCommandReturnSet execute(ExecutableCommandReturnSet returnSet, ClientPacket packet) {
        returnSet.response = ServerResponse.PONG;
        returnSet.arguments = packet.getArguments();

        return returnSet;
    }
}
