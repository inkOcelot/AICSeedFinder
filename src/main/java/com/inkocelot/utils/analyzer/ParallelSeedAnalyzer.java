package com.inkocelot.utils.analyzer;

import com.inkocelot.model.Chunk;
import com.inkocelot.model.Request;
import com.inkocelot.model.Seed;
import com.inkocelot.utils.factory.SeedFactory;
import com.inkocelot.utils.file.SlidingWindowLittleEndianReader;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
public class ParallelSeedAnalyzer {
    private static final int DEFAULT_THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors();

    private static List<Seed> processChunk(int order, Request req, Chunk chunk) {
        try (var reader = new SlidingWindowLittleEndianReader(
                Path.of(req.getPath()),
                req.getSlidingWindowSize() * 1024 * 1024
        )) {
            reader.seek(chunk.getStart()); // 定位到区块起始位置

            var topSeeds = new PriorityQueue<>(req.getSize(), Comparator.comparingInt(Seed::getScore));
            var remainSeedNumber = req.getSize() - (req.getParallelNumber() - 1);

            while (reader.getPosition() < chunk.getEnd()) {
                try {
                    var seed = SeedFactory.createFromFile(reader, req.getCond());
                    topSeeds.add(seed);
                    if (topSeeds.size() > remainSeedNumber) {
                        topSeeds.poll();
                    }
                } catch (IOException e) {
                    var errorPosition = reader.getPosition();
                    log.error("种子在 [0x{}] (区块偏移量: 0x{}) 处处理失败, 原因 - {}",
                            Long.toHexString(errorPosition),
                            Long.toHexString(errorPosition - chunk.getStart()),
                            e.getMessage(),
                            e);
                    break;
                }
            }
            log.info("区块{} - {}处理完毕", order, chunk);
            return topSeeds.stream().toList();
        } catch (IOException e) {
            log.error("处理单个区块时出现错误 - 区块{}", chunk, e);
            throw new RuntimeException(e);
        }
    }

    public static List<Seed> analyzer(Request req) throws IOException, ExecutionException, InterruptedException {
        // 准备线程池
        var executor = Executors.newFixedThreadPool(
                req.getParallelNumber() > 0 ?
                        Math.min(req.getParallelNumber(), DEFAULT_THREAD_POOL_SIZE) :
                        DEFAULT_THREAD_POOL_SIZE
        );

        // 文件分块
        var chunks = ChunkSplitter.split(Path.of(req.getPath()), req.getParallelNumber());

        // 创建并行任务
        var futures = new ArrayList<Future<List<Seed>>>();
        int order = 1;
        for (Chunk chunk : chunks) {
            final int currentOrder = order;
            futures.add(executor.submit(() -> processChunk(currentOrder, req, chunk)));
            order++;
        }

        // 收集并合并结果
        var result = new ArrayList<Seed>();
        for (Future<List<Seed>> future : futures) {
                result.addAll(future.get());
        }

        // 关闭线程池
        executor.shutdown();

        // 返回结果
        return result.stream()
                .sorted(Comparator.comparingInt(Seed::getScore).reversed())
                .limit(req.getSize())
                .toList();
    }
}