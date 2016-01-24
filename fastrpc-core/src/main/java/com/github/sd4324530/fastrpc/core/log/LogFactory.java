package com.github.sd4324530.fastrpc.core.log;

import com.github.sd4324530.fastrpc.core.log.jdk.JdkLog;

import java.util.HashMap;
import java.util.Map;

/**
 * @author peiyu
 */
public final class LogFactory {

    private LogFactory(){}

    private static final Map<String, Log> LOG_MAP = new HashMap<>();
    private static final Object LOCK = new Object();

    public static Log get(final Class clazz) {
        Log log = LOG_MAP.get(clazz.getName());
        if(null == log) {
            synchronized (LOCK) {
                log = LOG_MAP.get(clazz.getName());
                if(null == log) {
                    log = new JdkLog(clazz);
                    LOG_MAP.put(clazz.getName(), log);
                }
            }
        }
        return log;
    }

}
