package com.inkocelot.utils.file;

import java.io.IOException;

public interface LittleEndianReader {
    int readInt() throws IOException;
    byte readByte() throws IOException;
    int readUnsignedByte() throws IOException;
    void readFully(byte[] dst) throws IOException;
    long getPosition();
}