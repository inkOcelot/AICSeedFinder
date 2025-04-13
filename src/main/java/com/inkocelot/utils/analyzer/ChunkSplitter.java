package com.inkocelot.utils.analyzer;

import com.inkocelot.model.Chunk;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public final class ChunkSplitter {

    private static final byte[] START_MAGIC_LE = {0x78, 0x56, 0x34, 0x12};
    private static final byte[] END_MAGIC_LE = {0x21, 0x43, 0x65, (byte) 0x87};
    private static final int MAGIC_LENGTH = 4;

    public static List<Chunk> split(Path filePath, int numChunks) throws IOException {
        var chunks = new ArrayList<Chunk>();
        try (FileChannel channel = FileChannel.open(filePath, StandardOpenOption.READ)) {
            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());

            var fileSize = buffer.limit();
            var chunkSize = fileSize / numChunks;
            var currentStart = 0L;

            for (int i = 0; i < numChunks; i++) {
                var nominalEnd = (i == numChunks - 1) ? fileSize : currentStart + chunkSize;

                // 向后搜索终止魔数
                var endPos = scanMagic(buffer, nominalEnd, END_MAGIC_LE);
                if (endPos == -1) {
                    chunks.add(new Chunk(currentStart, fileSize - 1));
                    break;
                }

                // 向前搜索起始魔数
                var nextStart = scanMagic(buffer, endPos + MAGIC_LENGTH, START_MAGIC_LE);
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

    private static long scanMagic(MappedByteBuffer buffer, long fromPos, byte[] magic) {
        for (var i = fromPos; i <= buffer.limit() - magic.length; i++) {
            var match = true;
            for (var j = 0; j < magic.length; j++) {
                if (buffer.get((int)(i + j)) != magic[j]) {
                    match = false;
                    break;
                }
            }
            if (match) return i;
        }
        return -1;
    }
}