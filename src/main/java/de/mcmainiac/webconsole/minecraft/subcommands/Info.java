package de.mcmainiac.webconsole.minecraft.subcommands;

import de.mcmainiac.webconsole.Main;
import de.mcmainiac.webconsole.server.Server;
import org.bukkit.command.CommandSender;

public class Info {
    public static void execute(CommandSender sender) {
        Server server = Main.getWebConsoleServer();
        sender.sendMessage("WebConsole by MCMainiac");
        sender.sendMessage("------------");
        sender.sendMessage("WebConsole server is currently " + server.getState().toString().toLowerCase());
        sender.sendMessage("There are " + server.getNumConnectedClients() + " client(s) connected.");
    }
}
