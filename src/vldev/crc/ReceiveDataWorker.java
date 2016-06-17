package vldev.crc;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class ReceiveDataWorker implements Runnable {

    private final Object lock = new Object();
    private final byte[] buffer = new byte[1024];
    private final Socket socket;
    private InputStream is;
    private volatile boolean running;
    private Thread workerThread;
    private Callback callback;

    public ReceiveDataWorker(Socket socket) {
        this.socket = socket;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void start() throws IOException {
        if (running) return;
        synchronized (lock) {
            if (running) return;
            is = socket.getInputStream();
            running = true;
            workerThread = new Thread(this);
            workerThread.start();
        }
    }

    public void stop() {
        if (!running) return;
        synchronized (lock) {
            if (!running) return;
            running = false;
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Logger.error(e);
                }
            }
            if (workerThread != null && !workerThread.equals(Thread.currentThread())) {
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
                int count = is.read(buffer);
                if (count == -1) {
                    throw new IOException("remote end has been closed");
                }
                if (callback != null) {
                    callback.onDataReceived(new String(buffer, 0, count));
                }
            } catch (IOException e) {
                Logger.error(e);
                if (callback != null) {
                    callback.onReceiveDataError(e);
                }
                break;
            }
        }
    }

    public interface Callback {
        void onDataReceived(String data);

        void onReceiveDataError(Exception e);
    }
}
