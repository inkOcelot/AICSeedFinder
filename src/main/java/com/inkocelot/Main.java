package com.inkocelot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inkocelot.model.Request;
import com.inkocelot.model.Respond;
import com.inkocelot.model.Seed;
import com.inkocelot.model.mode.SingleThreadConf;
import com.inkocelot.model.mode.SlidingWindowMappedConf;
import com.inkocelot.utils.ClipboardFilePaths;
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

        // 获取剪贴板中文件路径
        get("/path", (req, res) -> {
            res.type("application/json");

            try {
                var path = ClipboardFilePaths.get();

                // 检查路径是否为空
                if (path == null) {
                    res.status(500);
                    return mapper.writeValueAsString(new Respond(
                            false, "请求失败, path可能为空", null, null
                    ));
                }

                // 检查文件后缀
                if (!path.endsWith(".aicseed")) {
                    res.status(500);
                    return mapper.writeValueAsString(new Respond(
                            false, "文件的后缀错误", null, null
                    ));
                }

                // 成功返回路径
                return mapper.writeValueAsString(new Respond(
                        true, "请求成功", null, Map.of("path", path)
                ));

            } catch (Exception e) {
                res.status(500);
                log.warn("读取文件路径时出现错误", e);
                return mapper.writeValueAsString(new Respond(
                        false, "请求失败, 错误: " + e.getMessage(), null, null
                ));
            }
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
                        "接收到请求 - 服务器模式: {}, 解析文件路径: {}, 堆大小: {}",
                        request.getType(), request.getPath(), request.getSize()
                );

                // 主要业务逻辑
                var start = System.currentTimeMillis();
                List<Seed> result;
                if (request.getConf() instanceof SingleThreadConf stc) {
                    // 01 单线程
                    log.info("解析模式: (1)单线程, 缓冲大小: {}MB", stc.getBuffer());
                    result = SeedAnalyzer.analyzer(request);
                } else if (request.getConf() instanceof SlidingWindowMappedConf swmc) {
                    // 02 多线程分块滑动窗口内存映射
                    log.info(
                            "解析模式: (2)多线程分块内存映射, 线程数: {}, 窗口大小: {}MB, 分区缓冲大小: {}MB, 缓冲大小: {}MB",
                            swmc.getThreads(), swmc.getWindowSize(), swmc.getChunkBuffer(), swmc.getBuffer()
                    );
                    result = ParallelSeedAnalyzer.analyzer(request);
                } else {
                    res.status(500);
                    log.warn("未知的配置文件 - {}", request.getConf());
                    return mapper.writeValueAsString(
                            new Respond(false, "未知的配置文件", null, null)
                    );
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