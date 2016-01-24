package com.github.sd4324530.fastrpc.core.channel;

import com.github.sd4324530.fastrpc.core.message.RequestMessage;
import com.github.sd4324530.fastrpc.core.message.ResponseMessage;

import java.io.Serializable;

/**
 * @author peiyu
 */
public interface IChannel extends Serializable {

    /**
     * 获取通道ID
     *
     * @return 通道ID
     */
    String id();

    boolean isOpen();

    RequestMessage read();

    void write(ResponseMessage responseMessage);

}
