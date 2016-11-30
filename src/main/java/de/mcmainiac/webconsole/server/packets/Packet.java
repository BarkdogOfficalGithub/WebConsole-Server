package de.mcmainiac.webconsole.server.packets;

import java.util.ArrayList;

@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class Packet {
    public static final String ARGUMENTS_DELIMITER = " ";
    public static final byte[] PACKET_END = "\u007f".getBytes();
    public static final int PACKET_ID_LENGTH = 4;
    public static final int PACKET_COMMAND_PAYLOAD_LENGTH = 4;
    public static final int PACKET_ARGUMENTS_PAYLOAD_LENGTH = 4;
    public static final int MIN_PACKET_SIZE =
            PACKET_ID_LENGTH +
            PACKET_COMMAND_PAYLOAD_LENGTH +
            PACKET_ARGUMENTS_PAYLOAD_LENGTH; // no PACKET_END.length because it gets discarded when read
    public static final double MAX_PACKET_SIZE =
            MIN_PACKET_SIZE +
            Math.pow(16, PACKET_COMMAND_PAYLOAD_LENGTH) +
            Math.pow(16, PACKET_ARGUMENTS_PAYLOAD_LENGTH);

    int id;
    String commandString;
    ArrayList<String> arguments;

    public int getId() {
        return id;
    }

    public String getCommandString() {
        return commandString;
    }

    public ArrayList<String> getArguments() {
        return arguments;
    }

    public boolean hasArguments() {
        return arguments != null && arguments.size() > 0;
    }
}
