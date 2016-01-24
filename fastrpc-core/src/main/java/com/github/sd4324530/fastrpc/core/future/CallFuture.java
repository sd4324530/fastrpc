package com.github.sd4324530.fastrpc.core.future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Semaphore;

/**
 * 请求句柄
 * @author peiyu
 */
public class CallFuture<T> {

//    private final Log log = LogFactory.get(getClass());
    private final Logger log = LoggerFactory.getLogger(getClass());

    private T value;

    private final Semaphore semaphore;

    public CallFuture(){
        this.semaphore = new Semaphore(0);
    }

    public void setValue(T value) {
        this.value = value;
        this.semaphore.release();
    }

    public T getValue() {
        try {
            this.semaphore.acquire();
        } catch (InterruptedException e) {
            log.error("阻塞异常", e);
        }
        return this.value;
    }
}
