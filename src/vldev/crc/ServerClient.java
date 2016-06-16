package vldev.crc;

import java.io.IOException;
import java.net.Socket;

public class ServerClient implements SendDataWorker.Callback, ReceiveDataWorker.Callback {

    public static final int STATUS_CONNECTED = 1;
    public static final int STATUS_DISCONNECTED = 2;

    private final Socket socket;
    private final Server server;
    private final SendDataWorker sendDataWorker;
    private final ReceiveDataWorker receiveDataWorker;
    private final HeartbeatWorker heartbeatWorker;
    private final Object lock = new Object();
    private volatile int status;

    public ServerClient(Server server, Socket socket) throws IOException {
        Logger.info("client connected " + socket.getRemoteSocketAddress());

        status = STATUS_CONNECTED;

        this.server = server;
        this.socket = socket;

        sendDataWorker = new SendDataWorker(socket);
        sendDataWorker.setCallback(this);
        sendDataWorker.start();

        heartbeatWorker = new HeartbeatWorker(sendDataWorker, 30000);
        heartbeatWorker.start();

        receiveDataWorker = new ReceiveDataWorker(socket);
        receiveDataWorker.setCallback(this);
        receiveDataWorker.start();
    }

    public int getStatus() {
        return status;
    }

    public void close() {
        if (status == STATUS_DISCONNECTED) return;
        synchronized (lock) {
            if (status == STATUS_DISCONNECTED) return;
            status = STATUS_DISCONNECTED;
            try {
                if (heartbeatWorker != null) {
                    heartbeatWorker.stop();
                }
                if (sendDataWorker != null) {
                    sendDataWorker.stop();
                }
                if (receiveDataWorker != null) {
                    receiveDataWorker.stop();
                }
                socket.close();
                server.removeClient(this);
                Logger.info("client disconnected " + socket.getRemoteSocketAddress());
            } catch (IOException e) {
                Logger.error(e);
            }
        }
    }

    public void send(String data) throws IOException {
        sendDataWorker.send(data);
    }

    @Override
    public void onSendDataError(Exception e) {
        close();
    }

    @Override
    public void onDataReceived(String data) {
        Logger.info("data received: " + data.replace("\r\n", ""));
    }

    @Override
    public void onReceiveDataError(Exception e) {
        close();
    }
}
