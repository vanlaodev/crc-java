package vldev.crc;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class Logger {

    private Logger() {
    }

    public static void info(String msg) {
        System.out.println("[INFO] " + msg);
    }

    public static void error(String msg) {
        System.out.println("[ERROR] " + msg);
    }

    public static void error(Throwable throwable) {
        System.out.println("[ERROR] " + throwable.getMessage() + " - " + getStackTraceString(throwable));
    }

    private static String getStackTraceString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

}
