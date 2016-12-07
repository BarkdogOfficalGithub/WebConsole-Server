package de.mcmainiac.webconsole.server;

import de.mcmainiac.webconsole.server.commands.ClientCommand;
import de.mcmainiac.webconsole.server.commands.ExecutableCommand;
import de.mcmainiac.webconsole.server.commands.ExecutableCommandReturnSet;
import de.mcmainiac.webconsole.server.commands.impl.MC_Command;
import de.mcmainiac.webconsole.server.commands.impl.Ping;
import de.mcmainiac.webconsole.server.commands.impl.Quit;
import de.mcmainiac.webconsole.server.commands.impl.Undefined;
import de.mcmainiac.webconsole.server.packets.ClientPacket;
import de.mcmainiac.webconsole.server.packets.ServerPacket;
import de.mcmainiac.webconsole.server.pipeline.InputDecoder;
import de.mcmainiac.webconsole.server.pipeline.OutputEncoder;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class Channel implements Runnable {
    public static final int TIMEOUT = 20000; // = 20s

    private final int id;

    private Socket socket;
    private InetAddress address;
    private int port;
    private Server parent;
    private boolean closed = false;
    private ChannelGroup group;

    private InputDecoder reader = null;
    private OutputEncoder writer = null;
    //private int lastPacketId;

    /**
     * Create a new Channel
     *
     * @param nParent The {@link Server} on which the channel lies.
     * @param nSocket The {@link Socket} of the channel.
     *
     * @throws IOException When an IOException occurs
     */
    public Channel(Server nParent, Socket nSocket, ChannelGroup nGroup) throws IOException {
        parent = nParent;
        socket = nSocket;
        address = socket.getInetAddress();
        port = socket.getPort();
        group = nGroup;

        id = group.indexOf(this);

        // debug message
        //Main.log(toString() + " connected.");
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

            // keep reading until we no longer receive data
            while ((clientPacket = reader.readPacket()) != null) {
                // debug message
                //Main.log(clientPacket.toString());

                // update last packet id; NOT USED ATM
                //lastPacketId = clientPacket.getId();

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

    /**
     * Get the class of the local implementations of the executable commands
     *
     * @param command The command from the client
     *
     * @return The implementing command class
     */
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

    /**
     * Execute a command on the server.
     *
     * @param returnSet    The return set
     * @param command      The command to execute
     * @param clientPacket The original packet from the client
     *
     * @return The resulting server packet returned by the command
     */
    private ServerPacket executeCommand(ExecutableCommandReturnSet returnSet, ExecutableCommand command, ClientPacket clientPacket) {
        // the command has to modify the return set
        command.execute(this, returnSet, clientPacket);

        // create a new server packet containing the original packet id and the values from the return set
        return new ServerPacket(
                clientPacket.getId(),
                returnSet.response,
                returnSet.arguments
        );
    }

    /* NOT USED ATM
     * Send a text message to the client.
     *
     * @param message The message to send.
     *
     * @throws IOException When there is an error sending the packet.
     *//*
    void sendMessage(String message) throws IOException {
        ServerPacket packet = new ServerPacket(
                lastPacketId++,
                ServerResponse.MESSAGE,
                new ArrayList<>(Arrays.asList(message.split(Packet.ARGUMENTS_DELIMITER)))
        );

        sendPacket(packet);
    }*/

    /**
     * Send a packet to the connected client
     *
     * @param serverPacket The packet to send.
     *
     * @throws IOException When an error occurs while sending the packet.
     */
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

        // debug message
        //Main.log(toString() + " quit.");

        try {
            closed = true;
            socket.close();
        } finally {
            group.update();
        }
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
