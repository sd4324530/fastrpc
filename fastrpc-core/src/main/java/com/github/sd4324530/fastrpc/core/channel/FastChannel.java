package com.github.sd4324530.fastrpc.core.channel;

import com.github.sd4324530.fastrpc.core.message.RequestMessage;
import com.github.sd4324530.fastrpc.core.message.ResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.UUID;

/**
 * @author peiyu
 */
public class FastChannel implements IChannel {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final AsynchronousSocketChannel channel;
    private final String                    id;


    public FastChannel(AsynchronousSocketChannel channel) {
        this.channel = channel;
        this.id = UUID.randomUUID().toString().replaceAll("-", "");
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public boolean isOpen() {
        return this.channel.isOpen();
    }

    @Override
    public RequestMessage read() {
        if(this.isOpen()) {
            ByteBuffer messageLength = ByteBuffer.allocate(4);
            this.channel.read(messageLength);
            int length = messageLength.getInt();
            ByteBuffer message = ByteBuffer.allocate(length);
            this.channel.read(message);

        }
        return null;
    }

    @Override
    public void write(ResponseMessage responseMessage) {

    }

}
