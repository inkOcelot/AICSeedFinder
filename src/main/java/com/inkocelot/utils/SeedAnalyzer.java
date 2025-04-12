package com.inkocelot.utils;

import com.inkocelot.model.Request;
import com.inkocelot.model.Seed;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

@Slf4j
public class SeedAnalyzer {

    private SeedAnalyzer() {}

    public static List<Seed> analyzer(Request req) {
        try (var file = new FileInputStream(req.getPath())) {

            var reader = new LittleEndianDataReader(file, req.getBuffer());
            var topSeeds = new PriorityQueue<>(req.getSize(), Comparator.comparingInt(Seed::getScore));

            while (true) {
                try {
                    var seed = SeedFactory.createFromFile(reader, req.getCond());
                    topSeeds.add(seed);
                    if (topSeeds.size() > req.getSize()) {
                        topSeeds.poll();
                    }
                } catch (IOException e) {
                    break;
                }
            }

            return topSeeds.stream().toList();
        } catch (IOException e) {
            log.error("处理文件时出错 - {}", e.getMessage());
        }
        return null;
    }
}
