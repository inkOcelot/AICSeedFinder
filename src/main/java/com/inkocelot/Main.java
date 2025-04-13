package com.inkocelot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inkocelot.model.Request;
import com.inkocelot.model.Respond;
import com.inkocelot.model.Seed;
import com.inkocelot.utils.analyzer.ParallelSeedAnalyzer;
import com.inkocelot.utils.analyzer.SeedAnalyzer;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static spark.Spark.get;
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
        props.setProperty("server.port", "2641");

        // 保存默认值
        try (var output = new OutputStreamWriter(new FileOutputStream(CONFIG_FILE), StandardCharsets.UTF_8)) {
            props.store(output, "自动生成的默认配置");
        } catch (IOException ex) {
            log.error("无法保存配置文件", ex);
        }

        return props;
    }

    public static void main(String[] args) {
        // 查找配置文件
        var props = loadOrCreateConfig();
        var serverPort = Integer.parseInt(props.getProperty("server.port"));
        log.info("服务器已在端口{}上开启", serverPort);

        port(serverPort);

        // 获取核心数
        get("/cores", (req, res) -> {
            res.type("application/json");
            return mapper.writeValueAsString(new Respond(
                    true, "请求成功", null,
                    Map.of("cores", Runtime.getRuntime().availableProcessors())
            ));
        });

        // 筛选种子并评分
        post("/seedfinder", (req, res) -> {
            try {
                // 解析JSON
                var request = mapper.readValue(req.body(), Request.class);

                // 判断路径是否为空
                if (request.getPath() == null || request.getPath().isEmpty()) {
                    res.status(400);
                    log.warn("接收到的路径为空");
                    return mapper.writeValueAsString(
                            new Respond(false, "路径不可为空!", null, null)
                    );
                }

                log.info(
                        "接收到请求 => 解析模式: {}, 解析文件路径: {}, 多线程: {}, 线程数: {}, 滑动窗口大小: {}MB, 缓冲区大小: {}MB, 堆大小: {}",
                        request.getType(),
                        request.getPath(),
                        request.getIsParallel() ? "是" : "否",
                        request.getParallelNumber(),
                        request.getSlidingWindowSize(),
                        request.getBuffer(),
                        request.getSize()
                );

                // 主要业务逻辑
                var start = System.currentTimeMillis();
                List<Seed> result;
                if (request.getIsParallel()) {
                    result = ParallelSeedAnalyzer.analyzer(request);
                } else {
                    result = SeedAnalyzer.analyzer(request);
                }
                var duration = System.currentTimeMillis() - start;
                log.info("解析完成, 本次解析耗时{}ms", duration);

                // 返回请求
                res.type("application/json");
                return mapper.writeValueAsString(
                        new Respond(true, "解析成功", duration, result)
                );
            } catch (Exception e) {
                // 解析时发生错误
                res.status(500);
                log.warn("解析时发生错误 - {}", e.getMessage(), e);
                return mapper.writeValueAsString(
                        new Respond(false, "错误: " + e.getMessage(), null, null)
                );
            }
        });
    }
}