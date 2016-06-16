package vldev.crc;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client implements SendDataWorker.Callback, ReceiveDataWorker.Callback {

    public static final int STATUS_CONNECTED = 1;
    public static final int STATUS_DISCONNECTED = 2;
    public static final int STATUS_CONNECTING = 3;
    private final Object lock = new Object();
    private Socket socket;
    private SendDataWorker sendDataWorker;
    private ReceiveDataWorker receiveDataWorker;
    private HeartbeatWorker heartbeatWorker;
    private AutoReconnectWorker autoReconnectWorker;
    private volatile int status;
    private InetSocketAddress lastSocketAddr;
    private boolean autoReconnect = true;

    public Client() {
        autoReconnectWorker = new AutoReconnectWorker(this, 5000);
    }

    public int getStatus() {
        return status;
    }

    public void connect() throws IOException {
        if (lastSocketAddr != null) {
            this.connect(lastSocketAddr.getHostName(), lastSocketAddr.getPort());
        }
    }

    public void connect(String host, int port) throws IOException {
        if (status == STATUS_CONNECTED || status == STATUS_CONNECTING) return;
        synchronized (lock) {
            if (status == STATUS_CONNECTED || status == STATUS_CONNECTING) return;
            status = STATUS_CONNECTING;
            lastSocketAddr = new InetSocketAddress(host, port);
            try {
                socket = new Socket(host, port);
                status = STATUS_CONNECTED;
                Logger.info("connected to " + socket.getRemoteSocketAddress());
                sendDataWorker = new SendDataWorker(socket);
                sendDataWorker.setCallback(this);
                sendDataWorker.start();

                heartbeatWorker = new HeartbeatWorker(sendDataWorker, 30000);
                heartbeatWorker.start();

                receiveDataWorker = new ReceiveDataWorker(socket);
                receiveDataWorker.setCallback(this);
                receiveDataWorker.start();
            } catch (IOException e) {
                Logger.error(e);
                close();
            }
        }
    }

    public void close() {
        if (status == STATUS_DISCONNECTED) return;
        synchronized (lock) {
            if (status == STATUS_DISCONNECTED) return;
            status = STATUS_DISCONNECTED;
            if (socket != null) {
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
                    Logger.info("disconnected from " + socket.getRemoteSocketAddress());
                } catch (IOException e) {
                    Logger.error(e);
                }
            }
            if (autoReconnectWorker != null && autoReconnect) {
                autoReconnectWorker.start();
            }
        }
    }

    public void dispose() {
        autoReconnect = false;
        if (autoReconnectWorker != null) {
            autoReconnectWorker.stop();
        }
        close();
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

    public void send(String data) {
        sendDataWorker.send(data);
    }
}
