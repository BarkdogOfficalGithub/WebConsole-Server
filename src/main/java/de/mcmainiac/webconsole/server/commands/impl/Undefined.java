package de.mcmainiac.webconsole.server.commands.impl;

import de.mcmainiac.webconsole.server.Channel;
import de.mcmainiac.webconsole.server.commands.ExecutableCommand;
import de.mcmainiac.webconsole.server.commands.ExecutableCommandReturnSet;
import de.mcmainiac.webconsole.server.commands.ServerResponse;
import de.mcmainiac.webconsole.server.packets.ClientPacket;

public class Undefined implements ExecutableCommand {
    @Override
    public void execute(Channel channel, ExecutableCommandReturnSet returnSet, ClientPacket packet) {
        returnSet.response = ServerResponse.UNDEFINED;
    }
}
