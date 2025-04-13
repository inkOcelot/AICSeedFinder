package com.inkocelot.utils.file;

import java.io.*;

/**
 * 实现了小端序(Little-Endian)二进制数据读取的{@link DataInput}类。
 * 该类包装了一个输入流，提供了以小端序格式读取基本数据类型的方法。
 *
 * @see LittleEndianReader
 * @see DataInput
 */
public class LittleEndianDataReader implements LittleEndianReader, DataInput {
    private final DataInput input;

    /**
     * 构造一个新的LittleEndianDataReader实例。
     *
     * @param in 要读取的输入流
     * @param buffer 缓冲区大小(以MB为单位)
     */
    public LittleEndianDataReader(InputStream in, int buffer) {
        this.input = new DataInputStream(new BufferedInputStream(in, buffer * 1024 * 1024));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int readUnsignedByte() throws IOException {
        return input.readUnsignedByte();
    }

    /**
     * 以小端序格式读取一个无符号16位short值。
     *
     * @return 作为int返回的无符号short值
     * @throws IOException 如果发生I/O错误
     */
    @Override
    public int readUnsignedShort() throws IOException {
        return Short.toUnsignedInt(readShort());
    }

    /**
     * 以小端序格式读取一个有符号16位short值。
     *
     * @return short值
     * @throws IOException 如果发生I/O错误
     */
    @Override
    public short readShort() throws IOException {
        return Short.reverseBytes(input.readShort());
    }

    /**
     * 以小端序格式读取一个有符号32位int值。
     *
     * @return int值
     * @throws IOException 如果发生I/O错误
     */
    @Override
    public int readInt() throws IOException {
        return Integer.reverseBytes(input.readInt());
    }

    /**
     * 以小端序格式读取一个有符号64位long值。
     *
     * @return long值
     * @throws IOException 如果发生I/O错误
     */
    @Override
    public long readLong() throws IOException {
        return Long.reverseBytes(input.readLong());
    }

    /**
     * 以小端序格式读取一个32位float值。
     *
     * @return float值
     * @throws IOException 如果发生I/O错误
     */
    @Override
    public float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    /**
     * 以小端序格式读取一个64位double值。
     *
     * @return double值
     * @throws IOException 如果发生I/O错误
     */
    @Override
    public double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    /**
     * 以小端序格式读取一个16位char值。
     *
     * @return char值
     * @throws IOException 如果发生I/O错误
     */
    @Override
    public char readChar() throws IOException {
        return Character.reverseBytes(input.readChar());
    }

    /**
     * 读取一个boolean值。
     *
     * @return boolean值
     * @throws IOException 如果发生I/O错误
     */
    @Override
    public boolean readBoolean() throws IOException {
        return input.readBoolean();
    }

    /**
     * 读取一个有符号8位byte值。
     *
     * @return byte值
     * @throws IOException 如果发生I/O错误
     */
    @Override
    public byte readByte() throws IOException {
        return input.readByte();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readFully(byte[] b) throws IOException {
        input.readFully(b);
    }

    /**
     * 获取当前在流中的位置。
     *
     * @return 在本实现中总是返回0
     */
    @Override
    public long getPosition() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        input.readFully(b, off, len);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int skipBytes(int n) throws IOException {
        return input.skipBytes(n);
    }

    /**
     * 不支持此操作。
     *
     * @throws UnsupportedOperationException 调用时总是抛出此异常
     */
    @Override
    public String readLine() {
        throw new UnsupportedOperationException("readLine not supported");
    }

    /**
     * 读取一个修改过的UTF-8格式的字符串。
     *
     * @return UTF字符串
     * @throws IOException 如果发生I/O错误
     */
    @Override
    public String readUTF() throws IOException {
        return input.readUTF();
    }
}