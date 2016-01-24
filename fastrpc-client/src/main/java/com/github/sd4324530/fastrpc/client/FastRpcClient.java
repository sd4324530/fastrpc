package com.github.sd4324530.fastrpc.client;

import com.github.sd4324530.fastrpc.core.message.RequestMessage;
import com.github.sd4324530.fastrpc.core.message.ResponseMessage;
import com.github.sd4324530.fastrpc.core.serializer.ISerializer;
import com.github.sd4324530.fastrpc.core.serializer.JdkSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * @author peiyu
 */
public final class FastRpcClient implements IClient {

    private final Logger      log        = LoggerFactory.getLogger(getClass());
    private       int         threadSize = Runtime.getRuntime().availableProcessors() * 2;
    private       ISerializer serializer = new JdkSerializer();
    private       long        timeout    = 5000;
    private       boolean     retry      = false;

    private AsynchronousChannelGroup  group;
    private AsynchronousSocketChannel channel;


    @Override
    public void connect(final SocketAddress address) throws IOException, InterruptedException, ExecutionException, TimeoutException {
        this.connect(address, false);
    }

    @Override
    public void connect(final SocketAddress address, final boolean retry) throws IOException, InterruptedException, ExecutionException, TimeoutException {
        this.group = AsynchronousChannelGroup.withFixedThreadPool(this.threadSize, Executors.defaultThreadFactory());
        this.channel = AsynchronousSocketChannel.open(this.group);
        this.channel.setOption(StandardSocketOptions.TCP_NODELAY, true);
        this.channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        this.channel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
        this.retry = retry;
        this.channel.connect(address).get(5, TimeUnit.SECONDS);
    }

    @Override
    public IClient threadSize(final int threadSize) {
        if (0 < threadSize) {
            this.threadSize = threadSize;
        } else {
            log.warn("threadSize must > 0!");
        }
        return this;
    }

    @Override
    public IClient serializer(final ISerializer serializer) {
        this.serializer = serializer;
        return this;
    }

    @Override
    public IClient setTimeout(final long timeout) {
        if (0 < timeout) {
            this.timeout = timeout;
        } else {
            log.warn("timeout must > 0!");
        }
        return this;
    }

    @Override
    public <T> T getService(final Class<T> clazz) {
        return this.getService(clazz.getSimpleName(), clazz);
    }

    @Override
    public <T> T getService(final String name, final Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, (proxy, method, args) -> {
            final RequestMessage requestMessage = new RequestMessage();
            requestMessage.setSeq(UUID.randomUUID().toString().replaceAll("-", ""));
            requestMessage.setServerName(name);
            requestMessage.setMethodName(method.getName());
            if (Objects.nonNull(args) && 0 != args.length) {
                requestMessage.setArgs(args);
                final Class[] argsClass = new Class[args.length];
                for (int i = 0; i < args.length; i++) {
                    argsClass[i] = args[i].getClass();
                }
                requestMessage.setArgsClassTypes(argsClass);
            }
            final ResponseMessage responseMessage = this.invoke(requestMessage);
            if (responseMessage.getResultCode() != 0) {
                throw new RuntimeException(responseMessage.getErrorMessage());
            }
            return responseMessage.getResponseObject();
        });
    }

    @Override
    public ResponseMessage invoke(final RequestMessage requestMessage) {
        try {
            final byte[] requestBytes = this.serializer.decoder(requestMessage);
            final int length = requestBytes.length;
            final ByteBuffer requestBuffer = ByteBuffer.allocate(4 + length);
            requestBuffer.putInt(length);
            requestBuffer.put(requestBytes);
            requestBuffer.flip();
            final Integer integer = this.channel.write(requestBuffer).get();
            if(-1 == integer) {
                throw new RuntimeException("连接断了!");
            }
            return readResponse();
        } catch (final Exception e) {
            log.error("Rpc调用异常:", e);
            final ResponseMessage responseMessage = new ResponseMessage();
            responseMessage.setSeq(requestMessage.getSeq());
            responseMessage.setResultCode(9999);
            responseMessage.setErrorMessage(e.toString());
            return responseMessage;
        }
    }

    @Override
    public void close() throws IOException {
        this.channel.close();
        this.group.shutdownNow();
    }

    private ResponseMessage readResponse() throws Exception {
        final ByteBuffer messageLength = ByteBuffer.allocate(4);
        Integer integer = this.channel.read(messageLength).get(this.timeout, TimeUnit.MILLISECONDS);
        if(-1 == integer) {
            throw new RuntimeException("连接断了!");
        }
        messageLength.flip();
        final int length = messageLength.getInt();
        final ByteBuffer message = ByteBuffer.allocate(length);
        final Integer i = this.channel.read(message).get();
        if(-1 == i) {
            throw new RuntimeException("连接断了!");
        }
        message.flip();
        return this.serializer.encoder(message.array(), ResponseMessage.class);
    }

}
