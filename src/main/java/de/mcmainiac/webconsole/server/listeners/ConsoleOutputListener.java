package de.mcmainiac.webconsole.server.listeners;

import org.bukkit.plugin.Plugin;

import java.io.*;

public class ConsoleOutputListener {
    private static final File logFile = new File("console.log");

    public static void init(Plugin plugin) {
        OutputStream logFileOut;

        try {
            File pluginLogFile = new File(plugin.getDataFolder().toString(), logFile.getName());

            if (!pluginLogFile.getParentFile().exists())
                pluginLogFile.getParentFile().mkdirs();

            if (!pluginLogFile.exists())
                pluginLogFile.createNewFile();

            logFileOut = new FileOutputStream(pluginLogFile);
        } catch (IOException e) {
            return;
        }

        PrintStream out = new PrintStream(new LineCatchOutputStream(logFileOut));
        System.setOut(out);
    }

    private static class LineCatchOutputStream extends OutputStream {
        private final OutputStream systemOut = System.out;
        private final OutputStream lineOut;

        private StringBuilder builder = new StringBuilder();

        private LineCatchOutputStream(OutputStream lineOut) {
            this.lineOut = lineOut;
        }

        @Override
        public void write(int b) throws IOException {
            char curChar = (char) b;

            builder.append(curChar);
            systemOut.write(b);

            if (String.valueOf(curChar).equals("\n") || String.valueOf(curChar).equals("\r")) {
                // in this case, an empty line was printed
                printLn();
                return;
            }

            if (builder.length() > 1) {
                // create a new char array with a length of 1
                char[] lastCharAr = new char[1];

                // read last char from builder and store in array
                builder.getChars(builder.length() - 2, builder.length() - 1, lastCharAr, 0);

                // read char from array
                char lastChar = lastCharAr[0];

                // create a new string containing the last two chars
                String lastChars = new String(new char[]{curChar, lastChar});

                // check if it is a new line feed
                if (lastChars.equals("\r\n") || String.valueOf(lastChar).equals("\n") || String.valueOf(lastChar).equals("\r"))
                    printLn();
            }
        }

        private void printLn() throws IOException {
            String line = builder.toString();

            lineOut.write(line.getBytes());
            lineOut.flush();

            builder = new StringBuilder();
        }
    }
}
