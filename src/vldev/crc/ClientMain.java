package vldev.crc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ClientMain {

    public static void main(String[] args) {
        Client client = new Client();

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line;
        try {
            while ((line = br.readLine()) != null) {
                if (line.toLowerCase().startsWith("connect")) {
                    connect(client, line.substring(7));
                } else if (line.equalsIgnoreCase("hello")) {
                    client.send("Hello World!\r\n");
                } else if (line.equalsIgnoreCase("exit") || line.equalsIgnoreCase("quit")) {
                    break;
                } else if (line.equalsIgnoreCase("disconnect")) {
                    client.close();
                } else {
                    Logger.info("unknown command '" + line + "'");
                }
            }
        } catch (IOException e) {
            Logger.error(e);
        } finally {
            client.dispose();
        }
    }

    private static void connect(Client client, String addr) throws IOException {
        addr = addr.trim();
        String[] hostAndPort = addr.split(":");
        if (hostAndPort.length != 2) {
            throw new IllegalArgumentException("invalid host and/or port argument");
        }

        String host = hostAndPort[0].trim();
        int port = Integer.parseInt(hostAndPort[1].trim());

        client.connect(host, port);
    }

}
