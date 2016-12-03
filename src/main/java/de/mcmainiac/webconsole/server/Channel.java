package de.mcmainiac.webconsole.server;

import de.mcmainiac.webconsole.Main;
import de.mcmainiac.webconsole.server.channelpipeline.InputDecoder;
import de.mcmainiac.webconsole.server.channelpipeline.OutputEncoder;
import de.mcmainiac.webconsole.server.commands.ClientCommand;
import de.mcmainiac.webconsole.server.commands.ExecutableCommand;
import de.mcmainiac.webconsole.server.commands.ExecutableCommandReturnSet;
import de.mcmainiac.webconsole.server.commands.ServerResponse;
import de.mcmainiac.webconsole.server.commands.impl.MC_Command;
import de.mcmainiac.webconsole.server.commands.impl.Ping;
import de.mcmainiac.webconsole.server.commands.impl.Quit;
import de.mcmainiac.webconsole.server.commands.impl.Undefined;
import de.mcmainiac.webconsole.server.packets.ClientPacket;
import de.mcmainiac.webconsole.server.packets.Packet;
import de.mcmainiac.webconsole.server.packets.ServerPacket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class Channel implements Runnable {
    public static final int TIMEOUT = 1000 * 20;

    private final int id;

    private Socket socket;
    private InetAddress address;
    private int port;
    private Server parent;
    private boolean closed = false;

    private InputDecoder reader = null;
    private OutputEncoder writer = null;
    private int lastPacketId;

    /**
     * Create a new Channel
     *
     * @param nParent The {@link Server} on which the channel lies.
     * @param nSocket The {@link Socket} of the channel.
     *
     * @throws IOException When an IOException occurs
     */
    public Channel(Server nParent, Socket nSocket) throws IOException {
        parent = nParent;
        socket = nSocket;
        address = socket.getInetAddress();
        port = socket.getPort();

        parent.getChannels().add(this);

        id = parent.getChannels().indexOf(this);

        Main.log(toString() + " connected.");
    }

    /**
     * All of the client handling will happen here
     */
    @Override
    public void run() {
        try {
            reader = new InputDecoder(socket.getInputStream());
            writer = new OutputEncoder(socket.getOutputStream());

            ClientPacket clientPacket;
            ExecutableCommandReturnSet returnSet = new ExecutableCommandReturnSet();

            // TODO: keep connection alive until quit command is sent or a ping command fails
            // keep reading until we no longer receive data
            while ((clientPacket = reader.readPacket()) != null) {
                // debug message
                //Main.log(clientPacket.toString());

                // decode client command
                Class<? extends ExecutableCommand> commandClass = getCommandClass(clientPacket.getCommand());

                // create a new instance of the command
                ExecutableCommand command = commandClass.newInstance();

                // get server packet from executed command
                ServerPacket serverPacket = executeCommand(returnSet, command, clientPacket);

                // send newly created packet
                sendPacket(serverPacket);

                // if the command told to quit in the return set, break out of the while loop
                if (returnSet.quit)
                    break;

                // reset return set
                returnSet.reset();
            }

            close();
        } catch (Throwable throwable) {
            parent.exceptionOccurred(throwable, false, false);
        } finally {
            reader.close();
            try { writer.close(); }
            catch (IOException ignored) {}
        }
    }

    private Class<? extends ExecutableCommand> getCommandClass(ClientCommand command) {
        // decode the client command and map it to an implementation of the class
        switch (command) {
            case PING: return Ping.class;
            case QUIT: return Quit.class;
            case MC_COMMAND: return MC_Command.class;

            default:
            case UNDEFINED:
                return Undefined.class;
        }
    }

    private ServerPacket executeCommand(ExecutableCommandReturnSet returnSet, ExecutableCommand command, ClientPacket clientPacket) throws IOException {
        // the command has to modify the return set
        command.execute(returnSet, clientPacket);

        // create a new server packet containing the original packet id and the values from the return set
        return new ServerPacket(
                clientPacket.getId(),
                returnSet.response,
                returnSet.arguments
        );
    }

    public void sendMessage(String message) throws IOException {
        ServerPacket packet = new ServerPacket(
                lastPacketId++,
                ServerResponse.MESSAGE,
                new ArrayList<>(Arrays.asList(message.split(Packet.ARGUMENTS_DELIMITER)))
        );

        sendPacket(packet);
    }

    private void sendPacket(ServerPacket serverPacket) throws IOException {
        // debug message
        //Main.log(serverPacket.toString());

        writer.writePacket(serverPacket);
    }

    /**
     * Close this Channel.
     *
     * @throws IOException When an IO error occurs.
     */
    void close() throws IOException {
        if (closed)
            throw new IOException("This channel has already been closed!");

        Main.log(toString() + " quit.");

        closed = true;
        socket.close();
        parent.getChannels().remove(this);
    }

    /**
     * @return True if the socket, input and output streams are connected and this channel has not been closed yet.
     */
    boolean isConnected() {
        return !closed && !socket.isClosed() && !socket.isInputShutdown() && !socket.isOutputShutdown() && socket.isConnected();
    }

    /**
     * @return The string representation of this channel.
     */
    @Override
    public String toString() {
        return "Client [#" + id + "] " + address.toString().substring(1) + ":" + port + (!isConnected() ? " (disconnected)":"");
    }
}
