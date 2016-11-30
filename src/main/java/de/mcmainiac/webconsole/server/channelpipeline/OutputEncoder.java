package de.mcmainiac.webconsole.server.channelpipeline;

import de.mcmainiac.webconsole.server.packets.Packet;
import de.mcmainiac.webconsole.server.packets.ServerPacket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class OutputEncoder implements AutoCloseable {
    private static final Charset CHARSET = StandardCharsets.US_ASCII;
    private OutputStream os;

    public OutputEncoder(OutputStream out) {
        os = out;
    }

    public void writePacket(ServerPacket packet) throws IOException {
        // get id
        int id = packet.getId();
        byte[] idBytes = ByteBuffer
                .allocate(Packet.PACKET_ID_LENGTH)
                .putInt(id)
                .array();

        // get values from command
        String command = packet.getCommandString();
        byte[] commandBytes = command.getBytes(CHARSET);
        int commandLength = commandBytes.length;
        byte[] commandPayloadLength = ByteBuffer
                .allocate(Packet.PACKET_COMMAND_PAYLOAD_LENGTH)
                .putInt(commandLength)
                .array();

        // check if arguments where provided
        String arguments;
        byte[] argumentsBytes;
        if (packet.hasArguments()) {
            assert packet.getArguments() != null && packet.getArguments().size() > 0;

            arguments = String.join(Packet.ARGUMENTS_DELIMITER, packet.getArguments());
            argumentsBytes = arguments.getBytes(CHARSET);
        } else
            argumentsBytes = new byte[0];
        int argumentsStringLength = argumentsBytes.length;
        byte[] argumentsPayloadLength = ByteBuffer
                .allocate(Packet.PACKET_ARGUMENTS_PAYLOAD_LENGTH)
                .putInt(argumentsStringLength)
                .array();

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        buffer.write(idBytes);
        buffer.write(commandPayloadLength);
        buffer.write(argumentsPayloadLength);
        buffer.write(commandBytes);
        buffer.write(argumentsBytes);

        buffer.write(Packet.PACKET_END);

        byte[] b = buffer.toByteArray();

        os.write(b);
        os.flush();
    }

    /*private static String toHexString(byte[] bytes) {
        char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        char[] hexChars = new char[bytes.length * 3];
        int i, v;
        for (int j = 0; j < bytes.length; j++) {
            i = j * 3;
            v = bytes[j] & 0xFF;
            hexChars[i] = hexArray[v / 16];
            hexChars[i + 1] = hexArray[v % 16];
            hexChars[i + 2] = ' ';
        }
        return new String(hexChars);
    }*/

    @Override
    public void close() throws IOException {
        os.close();
    }
}
