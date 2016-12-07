package de.mcmainiac.webconsole.minecraft;

import de.mcmainiac.webconsole.Main;
import de.mcmainiac.webconsole.minecraft.subcommands.Info;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;

public class Commands {
    public static void execute(CommandSender sender, String label, String[] args) {
        switch (label) {
            case "info":
                Info.execute(sender);
                break;
            case "closeall":
                CloseAll(sender);
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Usage: /wcon [section [arguments...]]");
        }
    }

    private static void CloseAll(CommandSender sender) {
        try {
            if (sender instanceof Player)
                Main.log(sender.getName() + " is closing all channels.");

            Main.getWebConsoleServer().closeAllChannels();

            sender.sendMessage("All channels closed!");
        } catch (IOException e) {
            e.printStackTrace();
            sender.sendMessage(ChatColor.RED + "There was an error closing all channels!");
            sender.sendMessage(ChatColor.RED + "See console for more details.");

            Main.log("There was an error closing all channels:");
            e.printStackTrace();
        }
    }
}
