package vldev.crc;

import java.io.IOException;

public class AutoReconnectWorker implements Runnable {

    private final long interval;
    private final Object lock = new Object();
    private final Object wait = new Object();
    private volatile boolean running;
    private Thread workerThread;
    private Client client;

    public AutoReconnectWorker(Client client, long interval) {
        this.client = client;
        this.interval = interval;
    }

    public void start() {
        if (running) return;
        synchronized (lock) {
            if (running) return;
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
            if (workerThread != null && !workerThread.equals(Thread.currentThread())) {
                try {
                    synchronized (wait) {
                        wait.notify();
                    }
                    workerThread.join();
                } catch (InterruptedException e) {
                    Logger.error(e);
                }
            }
        }
    }

    @Override
    public void run() {
        while (running && client.getStatus() != Client.STATUS_CONNECTED) {
            try {
                if (client.getStatus() != Client.STATUS_CONNECTING && client.getStatus() != Client.STATUS_CONNECTED) {
                    Logger.info("reconnecting...");
                    client.connect();
                }
            } catch (IOException e) {
                Logger.error(e);
            }
            try {
                synchronized (wait) {
                    if (!(running && (client.getStatus() != Client.STATUS_CONNECTED))) break;
                    wait.wait(interval);
                }
            } catch (InterruptedException ignored) {

            }
        }
        stop();
    }
}
