package com.inkocelot.utils.analyzer;

import com.inkocelot.model.Request;
import com.inkocelot.model.Seed;
import com.inkocelot.model.mode.SingleThreadConf;
import com.inkocelot.utils.factory.SeedFactory;
import com.inkocelot.utils.file.LittleEndianDataReader;
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
        var conf = (SingleThreadConf) req.getConf();

        try (var file = new FileInputStream(req.getPath())) {

            var reader = new LittleEndianDataReader(file, conf.getBuffer());
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

            return topSeeds.stream().sorted(Comparator.comparingInt(Seed::getScore).reversed()).toList();
        } catch (IOException e) {
            log.error("处理文件时出错 - {}", e.getMessage());
        }
        return null;
    }
}

















