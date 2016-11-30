package de.mcmainiac.webconsole.server.listeners;

import de.mcmainiac.webconsole.server.Channel;
import de.mcmainiac.webconsole.server.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

/**
 * Listens for connections on a {@link ServerSocket}.
 */
public class ConnectionListener {
    private final ServerSocket socket;
    private final Server server;
    private final List<ServerEventListener> eventListeners;
    private boolean listen = false;

    /**
     * Create a new ConnectionListener to handle incoming connections.
     *
     * @param nSocket The {@link ServerSocket} of the server.
     */
    public ConnectionListener(Server nServer, ServerSocket nSocket, List<ServerEventListener> nEventListeners) {
        socket = nSocket;
        server = nServer;
        eventListeners = nEventListeners;
        listen = true;
    }

    /**
     * Start listening for new connections.
     */
    public void startListening() throws IOException {
        while (true) {
            // accept incoming connection
            Socket channelSocket;

            // in case we stop listening, catch the exception
            try {
                channelSocket = socket.accept();
            } catch (SocketException e) {
                // if we haven't stopped listening, throw the exception
                if (listen)
                    throw e;

                // start at the beginning of the loop
                continue;
            }

            // set timeout to the value specified in the Channel class
            channelSocket.setSoTimeout(Channel.TIMEOUT);

            // set Type of Service to IPTOS_RELIABILITY
            channelSocket.setTrafficClass(0x04);

            // other socket settings
            channelSocket.setKeepAlive(true);
            channelSocket.setTcpNoDelay(true);

            // start a new thread for the channel
            Thread t = new Thread(new Channel(server, channelSocket));
            t.start();

            for (ServerEventListener listener : eventListeners)
                listener.onClientConnected(channelSocket.getInetAddress(), channelSocket.getPort());
        }
    }

    /**
     * Stop listening for new connections.
     */
    public void stopListening() throws IOException {
        listen = false;

        socket.close();
    }
}
