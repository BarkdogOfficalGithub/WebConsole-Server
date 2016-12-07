package de.mcmainiac.webconsole.server.commands.impl;

import de.mcmainiac.webconsole.server.Channel;
import de.mcmainiac.webconsole.server.commands.ExecutableCommand;
import de.mcmainiac.webconsole.server.commands.ExecutableCommandReturnSet;
import de.mcmainiac.webconsole.server.commands.ServerResponse;
import de.mcmainiac.webconsole.server.packets.ClientPacket;
import de.mcmainiac.webconsole.server.packets.Packet;
import org.bukkit.Bukkit;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;

public class MC_Command implements ExecutableCommand {
    @Override
    public void execute(Channel channel, ExecutableCommandReturnSet returnSet, ClientPacket packet) {
        // the arguments hold the command to send
        // they must therefore consist of at least one entry
        if (packet.getArguments().size() >= 1) {
            // save original output and create new one
            PrintStream originalOut = System.out;
            ByteArrayOutputStream newOut = new ByteArrayOutputStream();

            // flush the output
            System.out.flush();

            // "hijack" the system out
            System.setOut(new PrintStream(newOut));

            // execute command
            Bukkit.getServer().dispatchCommand(
                    Bukkit.getConsoleSender(), // send command as console
                    String.join(" ", packet.getArguments()) // glue arguments together using ' '
            );

            // flush the output
            System.out.flush();

            // restore original system output
            System.setOut(originalOut);

            // get output
            String out = newOut.toString();
            out = out.trim();

            /*Main.log("Stolen output:");
            Main.log(out);*/

            // add output as arguments
            String[] outSplit = out.split(Packet.ARGUMENTS_DELIMITER);
            returnSet.arguments.addAll(Arrays.asList(outSplit));

            returnSet.response = ServerResponse.MC_COMMAND;
        } else {
            returnSet.arguments.addAll(
                    Arrays.asList(
                            "Arguments MUST contain at least one command!"
                                    .split(Packet.ARGUMENTS_DELIMITER)
                    )
            );

            returnSet.response = ServerResponse.ERROR;
        }
    }
}
