package vldev.crc;

public class HeartbeatWorker implements Runnable {

    private final long interval;
    private final SendDataWorker sendDataWorker;
    private final Object lock = new Object();
    private final Object wait = new Object();
    private volatile boolean running;
    private Thread workerThread;

    public HeartbeatWorker(SendDataWorker sendDataWorker, long interval) {
        this.sendDataWorker = sendDataWorker;
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
        while (running) {
            sendDataWorker.send("heartbeat\r\n");
            try {
                synchronized (wait) {
                    if (!running) break;
                    wait.wait(interval);
                }
            } catch (InterruptedException ignored) {
            }
        }
    }
}
