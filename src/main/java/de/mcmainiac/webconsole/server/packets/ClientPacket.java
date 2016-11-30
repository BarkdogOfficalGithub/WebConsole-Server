package de.mcmainiac.webconsole.server.packets;

import de.mcmainiac.webconsole.Main;
import de.mcmainiac.webconsole.server.commands.ClientCommand;
import de.mcmainiac.webconsole.server.exceptions.PacketFormatException;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class ClientPacket extends Packet {
    private ClientCommand command;

    /**
     * <h1>WebConsole Server Packet</h1>
     *
     * A packet has the following scheme:
     * <pre>
     *  _______________________________________ . . . _______ . . . ___
     * |  packet   |  command  | arguments |             |             |
     * |    id     |  length   |  length   |   command   |  arguments  |
     * |___________|___________|___________|___ . . . ___|___ . . . ___|
     * |           |           |           |             |             |
     * |  4 bytes  |  4 bytes  |  4 bytes  | command len |  args len   |
     * |___________|___________|___________|___ . . . ___|___ . . . ___|
     * |                                   |                           |
     * |             required              |          optional         |
     * |___________________________________|___ . . . _______ . . . ___|
     * </pre>
     *
     * <p>
     *     The packet id is an integer value (4 bytes), which is unique for this packet.
     * </p>
     *
     * <p>
     *     The number of bytes occupied by the command are specified as an integer value (4 bytes) in the command length
     *     section. Same goes for the arguments.
     * </p>
     *
     * <p>
     *     The command and the arguments length may be 0, so that the packet is only 12 bytes in total (this also
     *     defines the minimum length of the byte array). This is known to be the NULL-Packet, which can be used as
     *     some kind of ping-pong command.
     * </p>
     *
     * @param bytes The bytes of the received packet.
     *
     * @throws PacketFormatException When the bytes aren't formatted correctly.
     */
    public ClientPacket(byte[] bytes) throws PacketFormatException {
        int offset = 0;
        int minLength = MIN_PACKET_SIZE;

        // check minimal length in the beginning
        // since there has to be an id, command length and arguments length defined, all of those must be stored
        if (bytes.length < minLength) {
            Main.log("bytes: " + toHexString(bytes));
            throw new PacketFormatException("Packet size is " + bytes.length + " bytes whereas minimal length is " +
                    minLength);
        }

        // read the id-bytes and interpret them as an integer
        id = ByteBuffer.wrap(bytes, offset, PACKET_ID_LENGTH).getInt();

        // don't forget to shift the offset to the end of the id-bytes
        offset += PACKET_ID_LENGTH;

        // read the command length from the next {commandPayloadLength} bytes
        int commandLength = ByteBuffer.wrap(bytes, offset, PACKET_COMMAND_PAYLOAD_LENGTH).getInt();
        offset += PACKET_COMMAND_PAYLOAD_LENGTH;

        // also read the length of the arguments
        int argumentsLength = ByteBuffer.wrap(bytes, offset, PACKET_ARGUMENTS_PAYLOAD_LENGTH).getInt();
        offset += PACKET_ARGUMENTS_PAYLOAD_LENGTH;

        // at this point, the minimal length has increased the sum of the original minimum, the command and the
        // arguments length. Just check it here very quickly
        minLength += commandLength;
        minLength += argumentsLength;
        if (bytes.length < minLength)
            throw new PacketFormatException("Packet size is " + bytes.length + " bytes whereas minimal length is " +
                    minLength);

        // so at this point we know the lengths of each part of the packet

        // first, read out the command string
        commandString = new String(
                Arrays.copyOfRange(
                        bytes,
                        offset,
                        offset + commandLength),
                StandardCharsets.US_ASCII
        );

        // and immediately convert it into a ClientCommand
        command = ClientCommand.fromString(commandString);

        // don't forget the offset
        offset += commandLength;

        if (argumentsLength > 0) {
            // next, read out the arguments string
            String argumentsString = new String(
                    Arrays.copyOfRange(
                            bytes,
                            offset,
                            offset + argumentsLength),
                    StandardCharsets.US_ASCII
            );

            // the arguments are glued together with ' ' (space) characters; just split them
            arguments = new ArrayList<>(Arrays.asList(argumentsString.split(ARGUMENTS_DELIMITER)));
            arguments.trimToSize(); // minimize storage
        } else
            arguments = new ArrayList<>(0); // empty arguments list

        // The packet should have been completely read out and processed at this point
    }

    private static String toHexString(byte[] bytes) {
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
    }

    /**
     * @return The string representation of this packet.
     */
    @Override
    public String toString() {
        return "ClientPacket [id=" + getId() + "]: command=\"" + command.toString() + "\"  arguments=" + arguments.toString();
    }

    /**
     * @return The command derived from this packet.
     */
    public ClientCommand getCommand() {
        return command;
    }
}
