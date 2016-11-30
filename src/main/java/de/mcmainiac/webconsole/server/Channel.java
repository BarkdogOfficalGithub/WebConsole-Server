package de.mcmainiac.webconsole.server;

import de.mcmainiac.webconsole.Main;
import de.mcmainiac.webconsole.server.channelpipeline.InputDecoder;
import de.mcmainiac.webconsole.server.channelpipeline.OutputEncoder;
import de.mcmainiac.webconsole.server.commands.ClientCommand;
import de.mcmainiac.webconsole.server.commands.ExecutableCommand;
import de.mcmainiac.webconsole.server.commands.ExecutableCommandReturnSet;
import de.mcmainiac.webconsole.server.commands.impl.MC_Command;
import de.mcmainiac.webconsole.server.commands.impl.Ping;
import de.mcmainiac.webconsole.server.commands.impl.Quit;
import de.mcmainiac.webconsole.server.commands.impl.Undefined;
import de.mcmainiac.webconsole.server.packets.ClientPacket;
import de.mcmainiac.webconsole.server.packets.ServerPacket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class Channel implements Runnable {
    public static final int TIMEOUT = 1000 * 20;

    private final int id;

    private Socket socket;
    private InetAddress address;
    private int port;
    private Server parent;
    private boolean closed = false;

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
        try (
                InputDecoder reader = new InputDecoder(socket.getInputStream());
                OutputEncoder writer = new OutputEncoder(socket.getOutputStream())
        ) {
            ClientPacket clientPacket;
            ExecutableCommandReturnSet returnSet = new ExecutableCommandReturnSet();

            // keep reading until we no longer receive data
            while ((clientPacket = reader.readPacket()) != null) {
                // debug message
                //Main.log(clientPacket.toString());

                // decode client command
                Class<? extends ExecutableCommand> commandClass = getCommandClass(clientPacket.getCommand());

                // create a new instance of the command
                ExecutableCommand command = commandClass.newInstance();

                // execute the command; the command has to modify the return set
                command.execute(returnSet, clientPacket);

                // create a new server packet containing the original packet id and the values from the return set
                ServerPacket serverPacket = new ServerPacket(
                        clientPacket.getId(),
                        returnSet.response,
                        returnSet.arguments
                );

                // debug message
                //Main.log(serverPacket.toString());

                // send newly created packet
                writer.writePacket(serverPacket);

                // if the command told to quit in the return set, break out of the while loop
                if (returnSet.quit)
                    break;

                // reset return set
                returnSet.reset();
            }

            close();
        } catch (Throwable throwable) {
            parent.exceptionOccured(throwable, false, false);
        }
    }

    private Class<? extends ExecutableCommand> getCommandClass(ClientCommand command) {
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
