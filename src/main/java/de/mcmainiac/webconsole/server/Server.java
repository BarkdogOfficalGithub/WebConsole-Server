package de.mcmainiac.webconsole.server;

import de.mcmainiac.webconsole.server.exceptions.IllegalServerState;
import de.mcmainiac.webconsole.server.listeners.ConnectionListener;
import de.mcmainiac.webconsole.server.listeners.ServerEventListener;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

/**
 * A WebConsole server.
 */
public class Server implements Runnable {
    private final ArrayList<ServerEventListener> serverEventListeners = new ArrayList<>();
    // TODO: optimize channel organization; maybe use channel groups differently?
    private final ChannelGroup channels = new ChannelGroup();

    private Configuration configuration;
    private ServerState state = ServerState.UNDEFINED;
    private ServerState lastState = ServerState.UNDEFINED;

    private ServerSocket socket = null;
    private ConnectionListener connectionListener = null;

    /**
     * Create a new WebConsole server.
     *
     * @param nConfiguration The server configuration.
     */
    public Server(Configuration nConfiguration) {
        updateState(ServerState.STOPPED_NEVER_RAN);
        configuration = nConfiguration;
    }

    /**
     * Add a {@link ServerEventListener} to the server to get notified when something happens on the server.
     *
     * @param nListener An instance of an implementation of the {@link ServerEventListener} interface.
     */
    public void addEventListener(ServerEventListener nListener) {
        serverEventListeners.add(nListener);
    }

    /**
     * This method should be called by an extra thread.
     */
    public void run() {
        updateState(ServerState.STARTING);

        for (ServerEventListener listener : serverEventListeners)
            listener.onStartup();

        try {
            if (socket != null) {
                throw new IllegalServerState("Socket is not null!");
            }

            socket = new ServerSocket(
                    configuration.port,
                    configuration.backlog
            );

            connectionListener = new ConnectionListener(this, socket, serverEventListeners);

            updateState(ServerState.RUNNING);

            for (ServerEventListener listener : serverEventListeners)
                listener.onRunning();

            connectionListener.startListening();
        } catch (Throwable throwable) {
            exceptionOccured(throwable, true, true);
        } finally {
            shutdown();
        }
    }

    /**
     * Shut down the server.
     */
    public void shutdown() {
        try {
            updateState(ServerState.STOPPING);
            for (ServerEventListener listener : serverEventListeners)
                listener.onShutdown(state);

            closeAllChannels();

            if (connectionListener != null)
                connectionListener.stopListening();

            updateState(ServerState.STOPPED);
        } catch (Throwable throwable) {
            exceptionOccured(throwable, true, false);
        } finally {
            for (ServerEventListener listener : serverEventListeners)
                listener.onShutdown(state);
        }
    }

    void exceptionOccured(Throwable cause, boolean crash, boolean shutdown) {
        if (crash)
            updateState(ServerState.CRASHED);

        for (ServerEventListener listener : serverEventListeners)
            listener.onExceptionOccured(cause, state, lastState, crash, shutdown);

        if (shutdown)
            shutdown();
    }

    private void updateState(ServerState nState) {
        lastState = state;
        state = nState;

        /*for (ServerEventListener listener : serverEventListeners)
            listener.onServerStateChange(lastState, state);*/
    }

    public ServerState getState() {
        return state;
    }

    public int getNumConnectedClients() {
        int i = 0;

        for (Channel channel : channels)
            if (channel.isConnected())
                i++;

        return i;
    }

    ArrayList<Channel> getChannels() {
        return channels;
    }

    public void closeAllChannels() throws IOException {
        ChannelGroup closingChannels = channels.clone();

        for (Channel channel : closingChannels)
            channel.close();
    }

    /**
     * The server configuration.
     */
    public static class Configuration {
        private int port = 0;
        private int backlog = 50;

        /**
         * Create a new server configuration.
         *
         * @param nPort    The port which the {@link ServerSocket} will listen on.
         * @param nBacklog The maximum queue length for incoming connections.
         */
        public Configuration(int nPort, int nBacklog) {
            port = nPort;
            backlog = nBacklog;
        }
    }
}
