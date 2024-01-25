package ir.moke;

import ch.qos.logback.classic.Level;
import ch.qos.logback.core.rolling.RollingFileAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;

public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    static {
        System.out.println("Application Started");
    }

    public static void main(String[] args) throws FileNotFoundException {
        LoggerManager.registerConverter();
        LoggerManager.addConsoleLogger("ir.moke", Level.ALL, null);
        wroteLog();
        System.out.println("-------------- Add File Logger  -------------------------");
        LoggerManager.addFileLogger("ir.moke", Level.ALL, null, "/tmp/sample.%d{yyyy-MM-dd}.%i.log.gz", "10MB", "100MB", 3);
        wroteLog();
        System.out.println("-------------- Remove File Logger  -------------------------");
        LoggerManager.detachLoggerAppender("ir.moke", RollingFileAppender.class);
        System.out.println("--------------- After remove  ------------------------");
        wroteLog();

        System.out.println("--------------- Add Syslog  ------------------------");
        LoggerManager.addSyslogLogger("ir.moke", Level.ALL, "localhost", 514, "USER", null);
        wroteLog();
    }

    private static void wroteLog() {
        logger.info("INFO log");
        logger.warn("WARN log");
        logger.debug("DEBUG log");
        logger.error("ERROR log");
        logger.trace("TRACE log");
    }
}
