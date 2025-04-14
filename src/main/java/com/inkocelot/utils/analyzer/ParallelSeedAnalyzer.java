package com.inkocelot.utils.analyzer;

import com.inkocelot.model.Chunk;
import com.inkocelot.model.Request;
import com.inkocelot.model.Seed;
import com.inkocelot.model.cond.Conditions;
import com.inkocelot.model.mode.SlidingWindowMappedConf;
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

    private static List<Seed> processChunk(
            int order,
            Path path,
            SlidingWindowMappedConf conf,
            int size,
            Conditions cond,
            Chunk chunk
    ) {
        try (var reader = new SlidingWindowLittleEndianReader(
                path,
                conf.getWindowSize() * 1024 * 1024
        )) {
            reader.seek(chunk.getStart()); // 定位到区块起始位置

            var topSeeds = new PriorityQueue<>(size, Comparator.comparingInt(Seed::getScore));
            var remainSeedNumber = Math.max(size - (conf.getThreads() - 1), 1);

            while (reader.getPosition() < chunk.getEnd()) {
                try {
                    var seed = SeedFactory.createFromFile(reader, cond);
                    topSeeds.add(seed);
                    if (topSeeds.size() > remainSeedNumber) {
                        topSeeds.poll();
                    }
                } catch (IOException e) {
                    var errorPosition = reader.getPosition();
                    log.error("种子在 [0x{}] (区块偏移量: 0x{}) 处发生错误, 原因 - {}",
                            Long.toHexString(errorPosition),
                            Long.toHexString(errorPosition - chunk.getStart()),
                            e.getMessage(),
                            e);
                    break;
                }
            }
            log.info("\t区块#{}\t- {}处理完毕", order, chunk);
            return topSeeds.stream().toList();
        } catch (IOException e) {
            log.error("处理单个区块时出现错误 - 区块{}", chunk, e);
            throw new RuntimeException(e);
        }
    }

    public static List<Seed> analyzer(Request req) throws IOException, ExecutionException, InterruptedException {
        // 读取配置
        var conf = (SlidingWindowMappedConf) req.getConf();

        // 准备线程池
        var executor = Executors.newFixedThreadPool(
                conf.getThreads()
        );

        // 文件分块
        ChunkSplitter.setBufferSize(conf.getChunkBuffer());
        var chunks = ChunkSplitter.split(Path.of(req.getPath()), conf.getThreads());

        // 创建并行任务
        var futures = new ArrayList<Future<List<Seed>>>();
        int order = 1;
        for (Chunk chunk : chunks) {
            final int currentOrder = order;
            futures.add(executor.submit(() -> processChunk(
                    currentOrder, Path.of(req.getPath()), conf, req.getSize(), req.getCond(), chunk
            )));
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