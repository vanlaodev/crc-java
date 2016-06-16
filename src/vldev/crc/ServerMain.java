package vldev.crc;

import vldev.crc.loggers.ConsoleLoggerCore;
import vldev.crc.loggers.FileBasedLoggerCore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ServerMain {

    public static void main(String[] args) {
        Logger.installLoggerCore(new ConsoleLoggerCore());
        Logger.installLoggerCore(new FileBasedLoggerCore("server-logs"));

        if (args.length != 1) {
            Logger.error("missing port argument");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(args[0]);
        } catch (Exception e) {
            Logger.error(e);
            return;
        }

        Server server = new Server(port);

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line;
        try {
            server.Start();
            while ((line = br.readLine()) != null) {
                if (line.equalsIgnoreCase("start")) {
                    server.Start();
                } else if (line.equalsIgnoreCase("stop")) {
                    server.Stop();
                } else if (line.equalsIgnoreCase("clientCount")) {
                    Logger.info("client count: " + server.getClientCount());
                } else if (line.equalsIgnoreCase("helloAll")) {
                    for (ServerClient client : server.getClients()) {
                        client.send("Hello World!\r\n");
                    }
                } else if (line.equalsIgnoreCase("exit") || line.equalsIgnoreCase("quit")) {
                    break;
                } else {
                    Logger.info("unknown command '" + line + "'");
                }
            }
        } catch (IOException e) {
            Logger.error(e);
        } finally {
            server.Stop();
        }
    }
}
