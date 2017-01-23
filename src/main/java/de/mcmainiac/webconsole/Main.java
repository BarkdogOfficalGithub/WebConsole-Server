package de.mcmainiac.webconsole;

import de.mcmainiac.webconsole.minecraft.Commands;
import de.mcmainiac.webconsole.server.Server;
import de.mcmainiac.webconsole.server.ServerState;
import de.mcmainiac.webconsole.server.ShutdownReason;
import de.mcmainiac.webconsole.server.listeners.ConsoleOutputListener;
import de.mcmainiac.webconsole.server.listeners.ServerEventListener;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.InetAddress;

public class Main extends JavaPlugin {
    private final static ConsoleCommandSender console = Bukkit.getConsoleSender();
    private final static String pre = "[WebConsole] ";

    private static Server server;

    @Override
    public void onLoad() {
        int port = 1424;
        int backlog = 128;

        server = new Server(
                new Server.Configuration(
                        port,
                        backlog
                )
        );

        ConsoleOutputListener.init(this);
    }

    @Override
    public void onEnable() {
        server.addEventListener(new ServerEventListenerImpl());

        Thread serverThread = new Thread(server);
        serverThread.start();

        log("Plugin enabled!");
    }

    @Override
    public void onDisable() {
        if (server.getState().equals(ServerState.RUNNING))
            server.shutdown(ShutdownReason.EXTERNAL_SHUTDOWN);

        log("Plugin disabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
        if (!cmdLabel.equalsIgnoreCase("wcon"))
            return false;

        String label;
        String[] arguments = new String[args.length > 0 ? args.length - 1 : 0];

        if (args.length == 0)
            label = "info";
        else
            label = args[0];

        if (args.length > 0)
            System.arraycopy(args, 1, arguments, 0, args.length - 1);

        Commands.execute(sender, label, arguments);

        return true;
    }

    public static void log(String message) {
        console.sendMessage(pre + message);
    }

    public static Server getWebConsoleServer() {
        return server;
    }

    // IDEA: maybe exclude to own class file
    private class ServerEventListenerImpl implements ServerEventListener {
        public void onStartup() {
            log("Server is starting...");
        }

        public void onRunning() {
            log("Server is now running.");
        }

        public void onShutdown(ServerState state, ShutdownReason reason) {
            switch (state) {
                case RUNNING:
                    log("Server is shutting down because plugin gets disabled. (" + reason.toString() + ")");
                    break;
                case STOPPING:
                    log("Server is shutting down. (" + reason.toString() + ")");
                    break;
                case STOPPED:
                case STOPPED_NEVER_RAN:
                    log("Server is now stopped. (" + reason.toString() + ")");
                    break;
                default:
                    log("Unexpected server state on shutting down: " + state.toString() + " (" + reason.toString() + ")");
            }
        }

        public void onExceptionOccurred(Throwable cause, ServerState state, ServerState lastState, boolean willCrash, boolean willShutdown) {
            String message = "An exception occurred on the server!";

            if (willCrash)
                message += " Server will crash!";

            if (willShutdown)
                message += " Server will " + (willCrash ? "also " : "") + "shutdown!";

            log(message);
            cause.printStackTrace();

            if (willCrash || willShutdown)
                Bukkit.getPluginManager().disablePlugin(Main.this);
        }

        public void onServerStateChange(ServerState oldState, ServerState newState) {
            log("Server changed state [from: " + oldState.toString() + "] [to: " + newState.toString() + "]");
        }

        public void onClientConnected(InetAddress address, int remotePort) {
            log("Client connected: " + address.toString() + ":" + remotePort);
        }
    }
}
