package com.github.sd4324530.fastrpc.core.util;

import java.nio.ByteBuffer;

/**
 * @author peiyu
 */
public final class ByteBufferUtils {

    private ByteBufferUtils(){}

    /**
     * 获取双倍大小的bytebuffer对象
     * @param byteBuffer 需要扩大的对象
     * @return 扩大后的对象
     */
    public static ByteBuffer biggerBuffer(ByteBuffer byteBuffer) {
        ByteBuffer buffer;
        int capacity = byteBuffer.capacity();
        buffer = ByteBuffer.allocate(capacity * 2);
        buffer.put(byteBuffer);
        return buffer;
    }
}
