package de.mcmainiac.webconsole.server;

/**
 * The ServerState defines the state of a WebConsole server.
 */
public enum ServerState {
    /**
     * The state of the server is currently not defined.
     */
    UNDEFINED,

    /**
     * The server is stopped and has never been ran before.<br>
     * This is the normal state when a server is created.
     */
    STOPPED_NEVER_RAN,

    /**
     * The server is currently starting.
     */
    STARTING,

    /**
     * The server is currently running.
     */
    RUNNING,

    /**
     * The server is stopping.
     */
    STOPPING,

    /**
     * The server has been stopped
     */
    STOPPED,

    /**
     * The server has crashed!
     */
    CRASHED
}
