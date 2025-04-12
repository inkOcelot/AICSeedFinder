package com.inkocelot;

import static spark.Spark.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inkocelot.model.Request;
import com.inkocelot.model.Respond;
import com.inkocelot.utils.SeedAnalyzer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) {
        port(2641);
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
                        "接收到请求, 解析模式: {}, 解析文件路径: {}, 缓冲区大小: {}MB, 堆大小: {}",
                        request.getType(), request.getPath(), request.getBuffer(), request.getSize()
                );

                var start = System.currentTimeMillis();
                var result = SeedAnalyzer.analyzer(request);
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