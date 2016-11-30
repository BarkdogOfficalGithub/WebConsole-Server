package de.mcmainiac.webconsole;

import de.mcmainiac.webconsole.minecraft.Commands;
import de.mcmainiac.webconsole.server.Server;
import de.mcmainiac.webconsole.server.ServerState;
import de.mcmainiac.webconsole.server.listeners.ServerEventListener;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.InetAddress;
import java.util.Set;

public class Main extends JavaPlugin {
    private final static ConsoleCommandSender console = Bukkit.getConsoleSender();
    private final static String pre = "[WebConsole] ";

    private static Server server;

    @Override
    public void onEnable() {
        int port = 1424;
        int backlog = 128;

        server = new Server(
                new Server.Configuration(
                        port,
                        backlog
                )
        );

        server.addEventListener(new ServerEventListenerImpl());

        Thread serverThread = new Thread(server);
        serverThread.start();

        log("Plugin enabled!");
    }

    @Override
    public void onDisable() {
        if (server.getState().equals(ServerState.RUNNING))
            server.shutdown();

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

    private class ServerEventListenerImpl implements ServerEventListener {
        public void onStartup() {
            log("Server is starting...");
            sendToOps("Server is starting");
        }

        public void onRunning() {
            log("Server is now running.");
            sendToOps("Server is now running");
        }

        public void onShutdown(ServerState state) {
            sendToOps("Server is shutting down");
            switch (state) {
                case RUNNING:
                    log("Server is shutting down because plugin gets disabled.");
                    break;
                case STOPPING:
                    log("Server is shutting down.");
                    break;
                case STOPPED:
                case STOPPED_NEVER_RAN:
                    log("Server is now stopped.");
                    break;
                default:
                    log("Unexpected server state on shutting down: " + state.toString());
            }
        }

        public void onExceptionOccured(Throwable cause, ServerState state, ServerState lastState, boolean willCrash, boolean willShutdown) {
            String message = "An exception occured on the server!";

            if (willCrash)
                message += " Server will crash!";

            if (willShutdown)
                message += " Server will " + (willCrash ? "also " : "") + "shutdown!";

            log(message);
            sendToOps(message);
            cause.printStackTrace();

            if (willCrash || willShutdown)
                Bukkit.getPluginManager().disablePlugin(Main.this);
        }

        public void onClientConnected(InetAddress address, int remotePort) {

        }

        private void sendToOps(String message) {
            Set<OfflinePlayer> players = Bukkit.getOperators();
            for (OfflinePlayer player : players) {
                if (!player.isOnline())
                    continue;

                ((Player) player).sendMessage(pre + message);
            }
        }
    }
}
