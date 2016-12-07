package de.mcmainiac.webconsole.server.commands;

import com.sun.istack.internal.Nullable;
import de.mcmainiac.webconsole.server.Channel;
import de.mcmainiac.webconsole.server.packets.ClientPacket;

public interface ExecutableCommand {
    /**
     * The main method, which is called when an executable command is instantiated.
     *
     * @param channel   The channel that called with command.
     * @param returnSet The return set which is used to transfer a pack of data.
     * @param packet    The client packet which caused this command to be called. May be null.
     */
    void execute(Channel channel, ExecutableCommandReturnSet returnSet, @Nullable ClientPacket packet);
}
