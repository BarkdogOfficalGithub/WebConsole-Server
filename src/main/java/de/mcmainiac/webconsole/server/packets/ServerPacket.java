package de.mcmainiac.webconsole.server.packets;

import de.mcmainiac.webconsole.server.commands.ServerResponse;

import java.util.ArrayList;

public class ServerPacket extends Packet {
    private final ServerResponse response;

    public ServerPacket(int nId, ServerResponse nResponse, ArrayList<String> nArguments) {
        id = nId;
        response = nResponse;
        commandString = nResponse.toString();
        arguments = nArguments;

        arguments.trimToSize();
    }

    @Override
    public String toString() {
        assert response != null && arguments != null;

        return "ServerPacket [id=" + getId() + "]: response=\"" + response.toString() + "\" arguments=" + arguments.toString();
    }
}
