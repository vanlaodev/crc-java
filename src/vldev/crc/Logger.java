package vldev.crc;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public final class Logger {

    private static final List<ILoggerCore> lcs = new ArrayList<ILoggerCore>();

    private Logger() {
    }

    public static void installLoggerCore(ILoggerCore lc) {
        synchronized (lcs) {
            lcs.add(lc);
        }
    }

    public static void info(String msg) {
        for (ILoggerCore lc : lcs) {
            lc.info(msg);
        }
    }

    public static void error(String msg) {
        for (ILoggerCore lc : lcs) {
            lc.error(msg);
        }
    }

    public static void error(Throwable throwable) {
        error("[ERROR] " + throwable.getMessage() + " - " + getStackTraceString(throwable));
    }

    private static String getStackTraceString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

}
