package de.mcmainiac.webconsole.server.exceptions;

import java.io.IOException;

public class PacketFormatException extends IOException {
    public PacketFormatException(String message) {
        super(message);
    }
}
