package vldev.crc.loggers;

import vldev.crc.ILoggerCore;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileBasedLoggerCore implements ILoggerCore {

    private final File logDir;
    private final SimpleDateFormat sdfLogFileName = new SimpleDateFormat("yyyyMMdd");
    private final SimpleDateFormat sdfLogPrefix = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final Object lock = new Object();

    public FileBasedLoggerCore(String path) {
        logDir = new File(path);
        if (logDir.exists() && !logDir.isDirectory()) {
            throw new IllegalArgumentException("path must be directory");
        } else if (!logDir.exists()) {
            logDir.mkdirs();
        }
    }

    @Override
    public void info(String msg) {
        log("INFO", msg);
    }

    @Override
    public void error(String msg) {
        log("ERROR", msg);
    }

    private void log(String logType, String msg) {
        synchronized (lock) {
            Date now = new Date();
            File logFile = new File(logDir, logType + "-" + sdfLogFileName.format(now) + ".txt");
            FileWriter fw = null;
            try {
                if (!logFile.exists()) {
                    logFile.createNewFile();
                }
                fw = new FileWriter(logFile, true);
                fw.write(sdfLogPrefix.format(now) + " - " + msg + "\r\n");
                fw.flush();
            } catch (IOException ignored) {
            } finally {
                if (fw != null) {
                    try {
                        fw.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }
    }
}
