package de.mcmainiac.webconsole.server;

import de.mcmainiac.webconsole.server.exceptions.IllegalServerStateException;
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
                throw new IllegalServerStateException("Socket is not null!");
            }

            socket = new ServerSocket(
                    configuration.port,
                    configuration.backlog
            );

            connectionListener = new ConnectionListener(this, channels, socket, serverEventListeners);

            updateState(ServerState.RUNNING);

            for (ServerEventListener listener : serverEventListeners)
                listener.onRunning();

            connectionListener.startListening();
        } catch (Throwable throwable) {
            exceptionOccurred(throwable, true, true);
        } finally {
            shutdown(ShutdownReason.SERVER_SHUTDOWN);
        }
    }

    /**
     * Shut down the server.<br>
     * {@link ServerEventListener#onShutdown} will get called twice with the updated server state.
     */
    public void shutdown(ShutdownReason reason) {
        try {
            updateState(ServerState.STOPPING);
            for (ServerEventListener listener : serverEventListeners)
                listener.onShutdown(state, reason);

            closeAllChannels();

            if (connectionListener != null)
                connectionListener.stopListening();

            updateState(ServerState.STOPPED);
        } catch (Throwable throwable) {
            // do not shutdown because it will lead to an endless loop
            exceptionOccurred(throwable, true, false);
        } finally {
            for (ServerEventListener listener : serverEventListeners)
                listener.onShutdown(state, reason);
        }
    }

    /**
     * This method is invoked when an exception occurs anywhere on the server.<br>
     * {@link ServerEventListener#onExceptionOccurred} gets called on the listeners.
     *
     * @param cause    The exception goes here.
     * @param crash    Whether or not the server will crash because of this exception
     * @param shutdown Whether or not the server should shut down after notifying the listeners.
     */
    void exceptionOccurred(Throwable cause, boolean crash, boolean shutdown) {
        if (crash)
            updateState(ServerState.CRASHED);

        for (ServerEventListener listener : serverEventListeners)
            listener.onExceptionOccurred(cause, state, lastState, crash, shutdown);

        if (shutdown)
            shutdown(ShutdownReason.SERVER_CRASH);
    }

    /**
     * Update the current server state.
     *
     * @param nState The new state of the server
     */
    private void updateState(ServerState nState) {
        lastState = state;
        state = nState;

        for (ServerEventListener listener : serverEventListeners)
            listener.onServerStateChange(lastState, state);
    }

    /**
     * @return The current server state.
     */
    public ServerState getState() {
        return state;
    }

    /**
     * @return The number of connected clients.
     */
    public int getNumConnectedClients() {
        return channels.size();
    }

    /**
     * Closes all channels.
     *
     * @throws IOException If there is an error disconnecting from a channel.
     */
    public void closeAllChannels() throws IOException {
        channels.closeAll();
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
