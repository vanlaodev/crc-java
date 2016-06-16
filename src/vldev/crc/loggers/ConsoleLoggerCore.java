package vldev.crc.loggers;

import vldev.crc.ILoggerCore;

public class ConsoleLoggerCore implements ILoggerCore {
    private static void log(String msg) {
        System.out.println(msg);
    }

    @Override
    public void info(String msg) {
        log("[INFO] " + msg);
    }

    @Override
    public void error(String msg) {
        log("[ERROR] " + msg);
    }
}
