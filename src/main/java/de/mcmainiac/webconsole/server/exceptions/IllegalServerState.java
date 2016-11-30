package de.mcmainiac.webconsole.server.exceptions;

public class IllegalServerState extends Throwable {
    public IllegalServerState() {
        super();
    }

    public IllegalServerState(String message) {
        super(message);
    }
}
