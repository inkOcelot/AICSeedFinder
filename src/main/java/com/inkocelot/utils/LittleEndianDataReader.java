package com.inkocelot.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class LittleEndianDataReader implements DataInput {
    private final DataInput input;

    public LittleEndianDataReader(InputStream in, int buffer) {
        this.input = new DataInputStream(new BufferedInputStream(in, buffer * 1024 * 1024));
    }

    // 必须实现的DataInput方法
    @Override
    public int readUnsignedByte() throws IOException {
        return input.readUnsignedByte();
    }

    @Override
    public int readUnsignedShort() throws IOException {
        return Short.toUnsignedInt(readShort());
    }

    @Override
    public short readShort() throws IOException {
        return Short.reverseBytes(input.readShort());
    }

    @Override
    public int readInt() throws IOException {
        return Integer.reverseBytes(input.readInt());
    }

    @Override
    public long readLong() throws IOException {
        return Long.reverseBytes(input.readLong());
    }

    @Override
    public float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    @Override
    public double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    @Override
    public char readChar() throws IOException {
        return Character.reverseBytes(input.readChar());
    }

    @Override
    public boolean readBoolean() throws IOException {
        return input.readBoolean();
    }

    @Override
    public byte readByte() throws IOException {
        return input.readByte();
    }

    @Override
    public void readFully(byte[] b) throws IOException {
        input.readFully(b);
    }

    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        input.readFully(b, off, len);
    }

    @Override
    public int skipBytes(int n) throws IOException {
        return input.skipBytes(n);
    }

    @Override
    public String readLine(){
        throw new UnsupportedOperationException("readLine not supported");
    }

    @Override
    public String readUTF() throws IOException {
        return input.readUTF();
    }

    // 辅助方法：读取可变长度字符串
    public String readString() throws IOException {
        int len = readUnsignedByte();
        byte[] bytes = new byte[len];
        readFully(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}