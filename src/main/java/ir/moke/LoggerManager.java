package ir.moke;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.net.SyslogAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class LoggerManager {
    private static final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
    private static final String DEFAULT_PATTERN = "[%-5level] %date [%thread] %logger{10} [%file:%line] %msg%n";
    private static final String SYSLOG_PATTERN = "[%-5level] [%thread] %logger{10} [%file:%line] %msg";

    static {
        loggerContext.reset();
    }

    public static <T> void detachLoggerAppender(String name, Class<? extends Appender> clazz) {
        Logger logger = loggerContext.getLogger(name);
        if (logger != null) {
            Appender<ILoggingEvent> appender = logger.getAppender(clazz.getSimpleName());
            appender.stop();
            logger.detachAppender(clazz.getSimpleName());
            loggerContext.getLoggerList().remove(logger);
        }
    }

    public static void addOutputStreamLogger(OutputStream outputStream, String packageName, Level level, String pattern) {
        OutputStreamAppender<ILoggingEvent> outputStreamAppender = getOutputStreamAppender(outputStream, pattern);
        Logger log = loggerContext.getLogger(packageName);
        log.setAdditive(false);
        log.setLevel(level);
        log.addAppender(outputStreamAppender);
    }

    public static void addConsoleLogger(String packageName, Level level, String pattern) {
        ConsoleAppender<ILoggingEvent> consoleAppender = getConsoleAppender(pattern);
        Logger log = loggerContext.getLogger(packageName);
        log.setAdditive(false);
        log.setLevel(level);
        log.addAppender(consoleAppender);
    }

    public static void addSyslogLogger(String packageName, Level level, String host, int port, String facility, String pattern) {
        SyslogAppender syslogAppender = getSyslogAppender(host, port, facility, pattern);
        Logger log = loggerContext.getLogger(packageName);
        log.setAdditive(false);
        log.setLevel(level);
        log.addAppender(syslogAppender);
    }

    public static void addFileLogger(String packageName, Level level, String pattern, String filePattern, String maxFileSize, String totalSize, int maxHistory) {
        RollingFileAppender<ILoggingEvent> fileAppender = getFileAppender(pattern, filePattern, maxFileSize, totalSize, maxHistory);
        Logger log = loggerContext.getLogger(packageName);
        log.setAdditive(false);
        log.setLevel(level);
        log.addAppender(fileAppender);
    }

    private static RollingFileAppender<ILoggingEvent> getFileAppender(String pattern, String filePattern, String maxFileSize, String totalSize, int maxHistory) {
        PatternLayout patternLayout = getPatternLayout(pattern);
        LayoutWrappingEncoder<ILoggingEvent> encoder = getEncoder(patternLayout);

        RollingFileAppender<ILoggingEvent> logFileAppender = new RollingFileAppender<>();
        logFileAppender.setContext(loggerContext);
        logFileAppender.setName(RollingFileAppender.class.getSimpleName());
        logFileAppender.setEncoder(encoder);
        logFileAppender.setAppend(true);
        logFileAppender.setFile("/tmp/sample.log");

        SizeAndTimeBasedRollingPolicy<ILoggingEvent> logFilePolicy = new SizeAndTimeBasedRollingPolicy<>();
        logFilePolicy.setContext(loggerContext);
        logFilePolicy.setParent(logFileAppender);
        logFilePolicy.setFileNamePattern(filePattern);
        logFilePolicy.setMaxHistory(maxHistory);
        logFilePolicy.setTotalSizeCap(FileSize.valueOf(totalSize));
        logFilePolicy.setMaxFileSize(FileSize.valueOf(maxFileSize));
        logFilePolicy.start();

        logFileAppender.setRollingPolicy(logFilePolicy);
        logFileAppender.start();
        return logFileAppender;
    }

    private static ConsoleAppender<ILoggingEvent> getConsoleAppender(String pattern) {
        PatternLayout patternLayout = getPatternLayout(pattern);
        LayoutWrappingEncoder<ILoggingEvent> encoder = getEncoder(patternLayout);
        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setContext(loggerContext);
        consoleAppender.setName(ConsoleAppender.class.getSimpleName());
        consoleAppender.setEncoder(encoder);
        consoleAppender.start();
        return consoleAppender;
    }

    private static SyslogAppender getSyslogAppender(String host, int port, String facility, String pattern) {
        SyslogAppender syslogAppender = new SyslogAppender();
        syslogAppender.setContext(loggerContext);
        syslogAppender.setName(ConsoleAppender.class.getSimpleName());
        syslogAppender.setSyslogHost(host);
        syslogAppender.setPort(port);
        syslogAppender.setFacility(facility);
        syslogAppender.setSuffixPattern(pattern != null ? pattern : SYSLOG_PATTERN);
        syslogAppender.start();
        return syslogAppender;
    }

    private static OutputStreamAppender<ILoggingEvent> getOutputStreamAppender(OutputStream outputStream, String pattern) {
        PatternLayout patternLayout = getPatternLayout(pattern);
        LayoutWrappingEncoder<ILoggingEvent> encoder = getEncoder(patternLayout);
        OutputStreamAppender<ILoggingEvent> outputStreamAppender = new OutputStreamAppender<>();
        outputStreamAppender.setContext(loggerContext);
        outputStreamAppender.setName(OutputStreamAppender.class.getSimpleName());
        outputStreamAppender.setEncoder(encoder);
        outputStreamAppender.setOutputStream(outputStream);
        outputStreamAppender.start();
        return outputStreamAppender;
    }

    private static LayoutWrappingEncoder<ILoggingEvent> getEncoder(PatternLayout pattern) {
        LayoutWrappingEncoder<ILoggingEvent> encoder = new LayoutWrappingEncoder<>();
        encoder.setContext(loggerContext);
        encoder.setCharset(StandardCharsets.UTF_8);
        encoder.setLayout(pattern);
        return encoder;
    }

    private static PatternLayout getPatternLayout(String pattern) {
        PatternLayout patternLayout = new PatternLayout();
        patternLayout.setContext(loggerContext);
        patternLayout.setPattern(pattern != null ? pattern : DEFAULT_PATTERN);
        patternLayout.start();
        return patternLayout;
    }
}
