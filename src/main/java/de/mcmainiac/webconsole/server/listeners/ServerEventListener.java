package de.mcmainiac.webconsole.server.listeners;

import de.mcmainiac.webconsole.server.ServerState;
import de.mcmainiac.webconsole.server.ShutdownReason;

import java.net.InetAddress;

/**
 * This interface is used to report any changes on the server.
 */
public interface ServerEventListener {
    /**
     * This method is called before the server starts up.
     */
    void onStartup();

    /**
     * This method is called before the listener actually starts listening for incoming connections.
     */
    void onRunning();

    /**
     * This method is called <u>twice</u> during the server shutdown. One time before the server shuts down and one
     * after the server is shut down.
     *
     * @param state  The current server state; either CRASHED, STOPPING or STOPPED from the enum {@link ServerState}.
     * @param reason The reason the server has shut down. {@link ShutdownReason}
     */
    void onShutdown(ServerState state, ShutdownReason reason);

    /**
     * This method is called when the server (or a channel on the server) experiences an exception.
     *
     * @param cause        The cause of the exception.
     * @param state        The current state of the server.
     * @param lastState    The last state of the server.
     * @param willCrash    Whether or not the server will crash.
     * @param willShutdown Whether or not the server will shutdown because of the exception.
     */
    void onExceptionOccurred(Throwable cause, ServerState state, ServerState lastState, boolean willCrash, boolean willShutdown);

    /**
     * This method gets called every time the server changes it's state.
     *
     * @param oldState The old state of the server.
     * @param newState The updated, new state of the server.
     */
    void onServerStateChange(ServerState oldState, ServerState newState);

    /**
     * This method is invoked whenever a new channel connects to the server.
     *
     * @param address    The remote address of the channel.
     * @param remotePort The remote port of the channel.
     */
    void onClientConnected(InetAddress address, int remotePort);
}
