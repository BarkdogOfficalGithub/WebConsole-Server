package de.mcmainiac.webconsole.server.commands;

import java.util.ArrayList;

public final class ExecutableCommandReturnSet {
    public ServerResponse response = ServerResponse.UNDEFINED;
    public ArrayList<String> arguments = new ArrayList<>(0);
    public boolean quit = false;

    public void reset() {
        response = ServerResponse.UNDEFINED;
        arguments = new ArrayList<>(0);
        quit = false;
    }
}
