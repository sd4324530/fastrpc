package com.github.sd4324530.fastrpc.core.log.jdk;

import com.github.sd4324530.fastrpc.core.log.AbstractLog;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author peiyu
 */
public class JdkLog extends AbstractLog {

    private final Logger log;

    public JdkLog(final Class clazz) {
        this.log = Logger.getLogger(clazz.getName());
        this.log.addHandler(new FastNormalLogHandler());
        this.log.addHandler(new FastErrorLogHandler());
        this.log.setLevel(Level.ALL);
    }

    @Override
    public void info(final String msg) {
        this.log.fine(msg);
    }

    @Override
    public void debug(final String msg) {
        this.log.config(msg);
    }

    @Override
    public void warn(final String msg) {
        this.log.warning(msg);
    }

    @Override
    public void warn(final String msg, final Throwable throwable) {
        this.log.warning(msg);
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        for(StackTraceElement traceElement : stackTrace) {
            this.log.warning(traceElement.toString());
        }
    }

    @Override
    public void error(final String msg, final Throwable throwable) {
        this.log.severe(msg);
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        for(StackTraceElement traceElement : stackTrace) {
            this.log.severe(traceElement.toString());
        }
    }


}
