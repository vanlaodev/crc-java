package vldev.crc;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;
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
        info(null, msg);
    }

    public static void error(String msg) {
        error(null, msg);
    }

    public static void error(Throwable throwable) {
        error(null, throwable);
    }

    public static void info(Socket socket, String msg) {
        String addrPrefix = getAddrPrefix(socket);
        msg = addrPrefix + msg;
        List<ILoggerCore> lcsClone = getShadowLoggerCores();
        for (ILoggerCore lc : lcsClone) {
            lc.info(msg);
        }
    }

    public static void error(Socket socket, String msg) {
        String addrPrefix = getAddrPrefix(socket);
        msg = addrPrefix + msg;
        List<ILoggerCore> lcsClone = getShadowLoggerCores();
        for (ILoggerCore lc : lcsClone) {
            lc.error(msg);
        }
    }

    private static List<ILoggerCore> getShadowLoggerCores() {
        List<ILoggerCore> lcsClone;
        synchronized (lcs) {
            lcsClone = new ArrayList<ILoggerCore>(lcs);
        }
        return lcsClone;
    }

    public static void error(Socket socket, Throwable throwable) {
        error(socket, throwable.getMessage() + " - " + getStackTraceString(throwable));
    }

    private static String getAddrPrefix(Socket socket) {
        return socket == null ? "" : socket.getRemoteSocketAddress().toString() + " - ";
    }

    private static String getStackTraceString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

}
