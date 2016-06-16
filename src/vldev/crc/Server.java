package vldev.crc;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server implements Runnable {

    private final int port;
    private final Object lock = new Object();
    private final List<ServerClient> clients = new ArrayList<ServerClient>();
    private volatile boolean running;
    private Thread workerThread;
    private ServerSocket ss;

    public Server(int port) {
        this.port = port;
    }

    public List<ServerClient> getClients() {
        return new ArrayList<ServerClient>(clients);
    }

    public int getClientCount() {
        synchronized (clients) {
            return clients.size();
        }
    }

    public void Start() throws IOException {
        if (running) return;
        synchronized (lock) {
            if (running) return;
            ss = new ServerSocket(port);
            Logger.info("server listening on port " + port);
            running = true;
            workerThread = new Thread(this);
            workerThread.start();
        }
    }

    public void Stop() {
        if (!running) return;
        synchronized (lock) {
            if (!running) return;
            running = false;
            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                    Logger.error(e);
                }
            }
            List<ServerClient> cloneList = null;
            synchronized (clients) {
                cloneList = new ArrayList<ServerClient>(clients);
            }
            for (ServerClient c : cloneList) {
                c.close();
            }
            if (workerThread != null) {
                try {
                    workerThread.join();
                } catch (InterruptedException e) {
                    Logger.error(e);
                }
            }
        }
    }

    @Override
    public void run() {
        while (running) {
            try {
                Socket s = ss.accept();
                ServerClient client = new ServerClient(Server.this, s);
                synchronized (clients) {
                    clients.add(client);
                }
            } catch (IOException e) {
//                Logger.error(e);
            }
        }
    }

    public void removeClient(ServerClient client) {
        synchronized (clients) {
            if (clients.contains(client)) {
                clients.remove(client);
            }
        }
    }
}
