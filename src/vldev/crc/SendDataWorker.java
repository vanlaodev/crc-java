package vldev.crc;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SendDataWorker implements Runnable {

    private final Object lock = new Object();
    private final Queue<String> queue = new ConcurrentLinkedQueue<String>();
    private final Socket socket;
    private OutputStream os;
    private volatile boolean running;
    private Thread workerThread;
    private Callback callback;

    public SendDataWorker(Socket socket) {
        this.socket = socket;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void start() throws IOException {
        if (running) return;
        synchronized (lock) {
            if (running) return;
            os = socket.getOutputStream();
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
            synchronized (queue) {
                queue.clear();
                queue.notify();
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    Logger.error(socket, e);
                }
            }
            if (workerThread != null && !workerThread.equals(Thread.currentThread())) {
                try {
                    workerThread.join();
                } catch (InterruptedException e) {
                    Logger.error(socket, e);
                }
            }
        }
    }

    public void send(String data) {
        if (running) {
            synchronized (queue) {
                queue.add(data);
                queue.notify();
            }
        }
    }

    @Override
    public void run() {
        while (running) {
            String dataToBeSent = null;
            synchronized (queue) {
                if (queue.isEmpty()) {
                    try {
                        queue.wait();
                        if (!queue.isEmpty()) {
                            dataToBeSent = queue.remove();
                        }
                    } catch (InterruptedException e) {
                        Logger.error(socket, e);
                    }
                } else {
                    dataToBeSent = queue.remove();
                }
            }
            if (dataToBeSent != null) {
                try {
                    os.write(dataToBeSent.getBytes());
                    os.flush();
                } catch (IOException e) {
                    Logger.error(socket, e);
                    if (callback != null) {
                        callback.onSendDataError(e);
                    }
                    break;
                }
            }
        }
    }

    public interface Callback {
        void onSendDataError(Exception e);
    }
}
