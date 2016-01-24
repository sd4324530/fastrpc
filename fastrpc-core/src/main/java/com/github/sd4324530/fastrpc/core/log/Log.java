package com.github.sd4324530.fastrpc.core.log;

/**
 * @author peiyu
 */
public interface Log {

    void info(String msg);

    void debug(String msg);

    void warn(String msg);

    void warn(String msg, Throwable throwable);

    void error(String msg, Throwable throwable);
}
