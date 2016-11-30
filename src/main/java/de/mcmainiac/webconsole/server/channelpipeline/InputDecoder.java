package de.mcmainiac.webconsole.server.channelpipeline;

import de.mcmainiac.webconsole.server.packets.ClientPacket;
import de.mcmainiac.webconsole.server.packets.Packet;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class InputDecoder implements Closeable {
    private final Scanner scanner;

    public InputDecoder(InputStream in) {
        scanner = new Scanner(in);
        scanner.useDelimiter(new String(Packet.PACKET_END, StandardCharsets.US_ASCII));
    }

    public ClientPacket readPacket() throws IOException {
        if (!scanner.hasNext())
            return null;

        String binaryString = scanner.next();
        byte[] packetBytes = binaryString.getBytes();

        return new ClientPacket(packetBytes);
    }

    @Override
    public void close() {
        scanner.close();
    }

    /*public ClientPacket readPacket() throws IOException {
        StringBuilder builder = new StringBuilder();
        int charCode;

        while ((charCode = super.read()) != -1)
            builder.append((char) charCode);

        if (builder.length() == 0)
            return null;

        String binaryString = builder.toString();
        byte[] packetBytes = binaryString.getBytes();

        return new ClientPacket(packetBytes);
    }*/
}
