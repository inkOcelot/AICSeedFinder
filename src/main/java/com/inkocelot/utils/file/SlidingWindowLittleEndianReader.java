package com.inkocelot.utils.file;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * 滑动窗口小端序文件读取器，实现基于内存映射文件的滑动窗口读取机制，
 * 支持小端序(Little Endian)格式数据的读取。
 * <p>
 * 该类通过将大文件分割为多个窗口(内存映射区域)来高效读取文件内容，
 * 避免一次性映射整个大文件导致的内存问题。
 *
 * @implNote 实现了{@link LittleEndianReader}和{@link AutoCloseable}接口
 */
@Slf4j
public class SlidingWindowLittleEndianReader implements LittleEndianReader, AutoCloseable {
    /** 文件通道，用于文件操作 */
    private final FileChannel channel;
    /** 文件总大小(字节) */
    private final long fileSize;
    /** 滑动窗口大小(字节) */
    private final int windowSize;

    /** 当前映射的窗口缓冲区 */
    private MappedByteBuffer currentWindow;
    /** 当前窗口在文件中的起始位置 */
    private long windowStart;
    /** 当前读取位置(绝对位置) */
    private long currentPosition;

    /**
     * 构造函数，创建滑动窗口读取器实例
     *
     * @param path 要读取的文件路径
     * @param windowSize 滑动窗口大小(字节)，必须大于0
     * @throws IOException 如果打开文件失败或获取文件大小失败
     */
    public SlidingWindowLittleEndianReader(Path path, int windowSize) throws IOException {
        this.channel = FileChannel.open(path, StandardOpenOption.READ);
        this.fileSize = channel.size();
        this.windowSize = windowSize;
        this.windowStart = -1;  // 初始未映射
        this.currentPosition = 0;
    }

    /**
     * 读取一个32位整数(小端序)
     *
     * @return 读取的整数值
     * @throws IOException 如果读取超出文件末尾或发生I/O错误
     */
    @Override
    public int readInt() throws IOException {
        ensureAvailable(4);
        var value = currentWindow.getInt((int)(currentPosition - windowStart));
        currentPosition += 4;
        return value;
    }

    /**
     * 读取一个字节
     *
     * @return 读取的字节值
     * @throws IOException 如果读取超出文件末尾或发生I/O错误
     */
    @Override
    public byte readByte() throws IOException {
        ensureAvailable(1);
        byte value = currentWindow.get((int)(currentPosition - windowStart));
        currentPosition += 1;
        return value;
    }

    /**
     * 读取一个无符号字节(0-255)
     *
     * @return 读取的无符号字节值(0-255)
     * @throws IOException 如果读取超出文件末尾或发生I/O错误
     */
    @Override
    public int readUnsignedByte() throws IOException {
        return readByte() & 0xFF;
    }

    /**
     * 读取指定长度的字节数组
     *
     * @param dst 目标字节数组
     * @throws IOException 如果读取超出文件末尾或发生I/O错误
     * @throws IllegalArgumentException 如果dst为null
     */
    @Override
    public void readFully(byte[] dst) throws IOException {
        ensureAvailable(dst.length);

        int offset = 0;
        int remaining = dst.length;

        while (remaining > 0) {
            int positionInWindow = (int)(currentPosition - windowStart);
            int chunkSize = Math.min(remaining, currentWindow.limit() - positionInWindow);

            currentWindow.position(positionInWindow);
            currentWindow.get(dst, offset, chunkSize);

            offset += chunkSize;
            remaining -= chunkSize;
            currentPosition += chunkSize;

            if (remaining > 0) {
                seek(currentPosition);  // 需要映射下一个窗口
            }
        }
    }

    /**
     * 获取当前读取位置
     *
     * @return 当前读取位置(文件偏移量)
     */
    @Override
    public long getPosition() {
        return currentPosition;
    }

    /**
     * 移动读取位置到指定偏移量
     *
     * @param position 新的读取位置(文件偏移量)
     * @throws IOException 如果位置超出文件范围或发生I/O错误
     */
    public void seek(long position) throws IOException {
        if (position < 0 || position >= fileSize) {
            throw new IOException("位置 " + position + " 越界 [0, " + fileSize + ")");
        }

        if (currentWindow == null || position != windowStart) {
            remapWindow(position);
        }
        currentPosition = position;
    }

    /**
     * 重新映射窗口到新的起始位置
     *
     * @param newWindowStart 新窗口的起始位置(文件偏移量)
     * @throws IOException 如果映射失败
     */
    private void remapWindow(long newWindowStart) throws IOException {
        if (newWindowStart < 0 || newWindowStart >= fileSize) {
            throw new IOException("非法的窗口起始位置: " + newWindowStart);
        }

        // 释放旧窗口
        if (currentWindow != null) {
            currentWindow = null;
            System.gc();
        }

        // 计算窗口实际大小
        var windowEnd = Math.min(newWindowStart + windowSize, fileSize);
        var actualWindowSize = (int)(windowEnd - newWindowStart);

        // 映射新窗口
        currentWindow = channel.map(FileChannel.MapMode.READ_ONLY, newWindowStart, actualWindowSize);
        currentWindow.order(ByteOrder.LITTLE_ENDIAN);

        windowStart = newWindowStart;
    }

    /**
     * 确保有足够字节可供读取
     *
     * @param bytes 需要的字节数
     * @throws IOException 如果剩余字节不足或发生I/O错误
     */
    private void ensureAvailable(int bytes) throws IOException {
        var endPos = currentPosition + bytes;
        if (endPos > fileSize) throw new IOException("尝试读取文件末尾之外");

        if (currentWindow == null || endPos > windowStart + currentWindow.limit()) {
            seek(currentPosition);
        }
    }

    /**
     * 关闭读取器并释放所有资源
     *
     * @throws IOException 如果关闭文件通道失败
     */
    @Override
    public void close() throws IOException {
        try {
            if (currentWindow != null) {
                currentWindow = null;
            }
        } finally {
            if (channel != null) {
                channel.close();
            }
            System.gc();
        }
    }
}