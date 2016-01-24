package com.github.sd4324530.fastrpc.server;

import com.github.sd4324530.fastrpc.core.message.RequestMessage;
import com.github.sd4324530.fastrpc.core.message.ResponseMessage;
import com.github.sd4324530.fastrpc.core.serializer.ISerializer;
import com.github.sd4324530.fastrpc.core.serializer.JdkSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;

/**
 * RPC服务端实现
 *
 * @author peiyu
 */
public final class FastRpcServer implements IServer {

    private final Logger      log        = LoggerFactory.getLogger(getClass());
    private       int         threadSize = Runtime.getRuntime().availableProcessors() * 2;
    private       ISerializer serializer = new JdkSerializer();

    private       int                             port;
    private       AsynchronousChannelGroup        group;
    private       AsynchronousServerSocketChannel channel;
    private final Map<String, Object>             serverMap;

    public FastRpcServer() {
        this.serverMap = new HashMap<>();
    }

    @Override
    public IServer bind(final int port) {
        this.port = port;
        return this;
    }

    @Override
    public IServer threadSize(final int threadSize) {
        if (0 < threadSize) {
            this.threadSize = threadSize;
        } else {
            log.warn("threadSize must > 0!");
        }
        return this;
    }

    @Override
    public IServer register(final String name, final Object object) {
        Objects.requireNonNull(name, "server'name is null");
        Objects.requireNonNull(object, "server " + name + " is null");
        this.serverMap.put(name, object);
        return this;
    }

    @Override
    public IServer register(final Object object) {
        Objects.requireNonNull(object, "server is null");
        this.serverMap.put(object.getClass().getSimpleName(), object);
        return this;
    }

    @Override
    public IServer register(final Map<String, Object> serverMap) {
        Objects.requireNonNull(serverMap, "serverMap is null");
        serverMap.forEach(this::register);
        return this;
    }

    @Override
    public IServer serializer(final ISerializer serializer) {
        this.serializer = serializer;
        return this;
    }

    @Override
    public void start() throws IOException {
        log.debug("开始启动RPC服务端......");
        this.group = AsynchronousChannelGroup.withFixedThreadPool(this.threadSize, Executors.defaultThreadFactory());
        this.channel = AsynchronousServerSocketChannel
                .open(this.group)
                .setOption(StandardSocketOptions.SO_REUSEADDR, true)
                .bind(new InetSocketAddress("localhost", this.port));

        this.channel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
            @Override
            public void completed(AsynchronousSocketChannel result, Void attachment) {
                channel.accept(null, this);
                try {
                    log.debug("创建连接 {} <-> {}", result.getLocalAddress(), result.getRemoteAddress());
                } catch (IOException e) {
                    log.error("", e);
                }
                while (result.isOpen()) {
                    handler(result);
                }
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                log.error("通信失败", exc);
                try {
                    close();
                } catch (IOException e) {
                    log.error("关闭通道异常", e);
                }
            }
        });
    }

    @Override
    public void close() throws IOException {
        this.channel.close();
        this.group.shutdownNow();
    }

    private void handler(final AsynchronousSocketChannel result) {
        try {
            final ByteBuffer messageLength = ByteBuffer.allocate(4);
            final Integer i = result.read(messageLength).get();
            if (-1 == i) {
                log.debug("关闭连接 {} <-> {}", result.getLocalAddress(), result.getRemoteAddress());
                result.close();
                return;
            }
            messageLength.flip();
            final int length = messageLength.getInt();
            final ByteBuffer message = ByteBuffer.allocate(length);
            result.read(message);
            final byte[] messageBytes = message.array();
            final RequestMessage request = this.serializer.encoder(messageBytes, RequestMessage.class);
            final String serverName = request.getServerName();
            final Object obj = this.serverMap.get(serverName);
            final Method method = obj.getClass().getMethod(request.getMethodName(), request.getArgsClassTypes());
            final Object response = method.invoke(obj, request.getArgs());
            final ResponseMessage responseMessage = new ResponseMessage();
            responseMessage.setSeq(request.getSeq());
            responseMessage.setResultCode(0);
            responseMessage.setResponseObject(response);
            final byte[] bytes = this.serializer.decoder(responseMessage);
            final ByteBuffer buffer = ByteBuffer.allocate(4 + bytes.length);
            buffer.putInt(bytes.length);
            buffer.put(bytes);
            buffer.flip();
            result.write(buffer).get();
        } catch (final Exception e) {
            log.error("处理消息异常", e);
            final ResponseMessage responseMessage = new ResponseMessage();
            responseMessage.setResultCode(9999);
            responseMessage.setErrorMessage("处理消息异常:" + e.getMessage());
            byte[] bytes = new byte[0];
            try {
                bytes = this.serializer.decoder(responseMessage);
            } catch (Exception e1) {
                log.error("序列化异常", e1);
            }
            final ByteBuffer buffer = ByteBuffer.allocate(4 + bytes.length);
            buffer.putInt(bytes.length);
            buffer.put(bytes);
            try {
                result.write(buffer).get();
            } catch (Exception e1) {
                log.error("异常", e1);
            }
        }
    }
}
