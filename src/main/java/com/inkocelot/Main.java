package com.inkocelot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inkocelot.model.Request;
import com.inkocelot.model.Respond;
import com.inkocelot.model.Seed;
import com.inkocelot.utils.analyzer.ParallelSeedAnalyzer;
import com.inkocelot.utils.analyzer.SeedAnalyzer;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.List;
import java.util.Properties;

import static spark.Spark.port;
import static spark.Spark.post;

@Slf4j
public class Main {
    private static final String CONFIG_FILE = "config.properties";
    private static final ObjectMapper mapper = new ObjectMapper();

    private static Properties loadOrCreateConfig() {
        var props = new Properties();

        try {
            try (InputStream input = new FileInputStream(CONFIG_FILE)) {
                props.load(input);
                log.info("配置已加载");
                return props;
            }
        } catch (IOException e) {
            log.warn("未找到配置文件, 将创建默认配置");
        }

        // 配置默认值
        props.setProperty("server.port", "2471");

        // 保存默认值
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            props.store(output, "自动生成的默认配置");
        } catch (IOException ex) {
            log.error("无法保存配置文件", ex);
        }

        return props;
    }

    public static void main(String[] args) {
        // 查找配置文件
        var props = loadOrCreateConfig();

        port(Integer.parseInt(props.getProperty("server.port")));
        post("/seedfinder", (req, res) -> {
            try {
                var request = mapper.readValue(req.body(), Request.class);

                if (request.getPath() == null || request.getPath().isEmpty()) {
                    res.status(400);
                    log.warn("接收到的路径为空");
                    return mapper.writeValueAsString(
                            new Respond(false, "路径不可为空!", null, null)
                    );
                }

                log.info(
                        "接收到请求 => 解析模式: {}, 解析文件路径: {}, 是否启用多线程: {}, 线程数: {}, 滑动窗口大小: {}MB, 缓冲区大小: {}MB, 堆大小: {}",
                        request.getType(),
                        request.getPath(),
                        request.getIsParallel(),
                        Math.min(request.getParallelNumber(),
                                Runtime.getRuntime().availableProcessors()),
                        request.getSlidingWindowSize(),
                        request.getBuffer(),
                        request.getSize()
                );

                var start = System.currentTimeMillis();
                List<Seed> result;
                if (request.getIsParallel()) {
                    result = ParallelSeedAnalyzer.analyzer(request);
                } else {
                    result = SeedAnalyzer.analyzer(request);
                }
                var duration = System.currentTimeMillis() - start;

                log.info("本次解析耗时{}ms", duration);

                res.type("application/json");
                return mapper.writeValueAsString(
                        new Respond(true, "解析成功", duration, result)
                );
            } catch (Exception e) {
                res.status(500);
                log.warn("解析时发生错误 - {}", e.getMessage());
                return mapper.writeValueAsString(
                        new Respond(false, "错误: " + e.getMessage(), null, null)
                );
            }
        });
    }
}