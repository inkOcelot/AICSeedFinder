package com.inkocelot.utils.analyzer;

import com.inkocelot.model.Chunk;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public final class ChunkSplitter {

    private static final byte[] START_MAGIC_LE = {0x78, 0x56, 0x34, 0x12};
    private static final byte[] END_MAGIC_LE = {0x21, 0x43, 0x65, (byte) 0x87};
    private static final int MAGIC_LENGTH = 4;
    private static int BUFFER_SIZE = 8 * 1024 * 1024;

    public static void setBufferSize(int size) {
        BUFFER_SIZE = size * 1024 * 1024;
    }

    public static List<Chunk> split(Path filePath, int numChunks) throws IOException {

        List<Chunk> chunks = new ArrayList<>();
        try (FileChannel channel = FileChannel.open(filePath, StandardOpenOption.READ)) {
            long fileSize = channel.size();
            long chunkSize = fileSize / numChunks;
            long currentStart = 0L;

            for (int i = 0; i < numChunks; i++) {
                long nominalEnd = (i == numChunks - 1) ? fileSize : currentStart + chunkSize;

                // 向后搜索终止魔数
                long endPos = scanMagic(channel, nominalEnd, END_MAGIC_LE);
                if (endPos == -1) {
                    chunks.add(new Chunk(currentStart, fileSize - 1));
                    break;
                }

                // 向前搜索起始魔数
                long nextStart = scanMagic(channel, endPos + MAGIC_LENGTH, START_MAGIC_LE);
                if (nextStart == -1) {
                    chunks.add(new Chunk(currentStart, fileSize - 1));
                    break;
                }

                chunks.add(new Chunk(currentStart, endPos + MAGIC_LENGTH - 1));
                currentStart = nextStart;
            }
        }
        return chunks;
    }

    private static long scanMagic(FileChannel channel, long fromPos, byte[] magic) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        long currentPos = fromPos;

        while (currentPos < channel.size()) {
            buffer.clear();
            int bytesRead = channel.read(buffer, currentPos);
            if (bytesRead == -1) break;

            buffer.flip();
            for (int i = 0; i <= bytesRead - magic.length; i++) {
                boolean match = true;
                for (int j = 0; j < magic.length; j++) {
                    if (buffer.get(i + j) != magic[j]) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    return currentPos + i;
                }
            }
            currentPos += (bytesRead - magic.length + 1); // 滑动窗口
        }
        return -1;
    }
}