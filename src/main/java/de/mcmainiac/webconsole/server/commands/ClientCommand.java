package de.mcmainiac.webconsole.server.commands;

public enum ClientCommand {
    UNDEFINED,

    QUIT,
    PING,
    MC_COMMAND;

    public static ClientCommand fromString(String string) {
        string = string.toLowerCase();
        switch (string) {
            case "quit":
                return QUIT;
            case "ping":
                return PING;
            case "mc_command":
                return MC_COMMAND;

            default:
                return UNDEFINED;
        }
    }
}
