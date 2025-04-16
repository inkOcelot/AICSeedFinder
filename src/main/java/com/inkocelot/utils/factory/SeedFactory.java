package com.inkocelot.utils.factory;

import com.inkocelot.model.Enemy;
import com.inkocelot.model.Seed;
import com.inkocelot.model.cond.Conditions;
import com.inkocelot.utils.file.LittleEndianReader;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class SeedFactory {

    public static Seed createFromFile(
            LittleEndianReader reader,
            Conditions conditions
    ) throws IOException {
        if (reader.readInt() != 0x12345678) log.error("错误的文件格式 - 起始头丢失");

        var attempt = reader.readInt();
        var seed1 = new long[4];
        var seed2 = new long[4];
        var seed3 = new long[4];
        var enemies = new ArrayList<Enemy>();

        for (int i = 0; i < 4; i++) seed1[i] = reader.readInt() & 0xFFFFFFFFL;
        for (int i = 0; i < 4; i++) seed2[i] = reader.readInt() & 0xFFFFFFFFL;
        for (int i = 0; i < 4; i++) seed3[i] = reader.readInt() & 0xFFFFFFFFL;

        var count = reader.readInt();
        var score = 0;
        var seedRules = IntStream.range(0, conditions.getSeed().size())
                .boxed()
                .collect(Collectors.toMap(i -> i, conditions.getSeed()::get));

        var seedCount = new HashMap<Integer, Integer>();

        // Enemy计分 从1开始数
        for (int i = 0; i < count; i++) {
            var enemy = EnemyFactory.createFromFile(
                    reader, seedRules, conditions.getEnemy(), count - i
            );
            for (Integer seedMatch : enemy.getSeedMatches()) {
                seedCount.put(seedMatch, seedCount.getOrDefault(seedMatch, 1));
            }
            score += enemy.getScore();
            enemies.add(enemy);
        }

        // Seed计分
        for (var entry : seedCount.entrySet()) {
            var seedRuleId = entry.getKey();
            var currentCount = entry.getValue();
            var currentSeedRule = seedRules.get(seedRuleId);

            var result = switch (currentSeedRule.getOperator()) {
                case "eq" -> currentCount == currentSeedRule.getValue();
                case "neq" -> currentCount != currentSeedRule.getValue();
                case "lt" -> currentCount < currentSeedRule.getValue();
                case "lte" -> currentCount <= currentSeedRule.getValue();
                case "gt" -> currentCount > currentSeedRule.getValue();
                case "gte" -> currentCount >= currentSeedRule.getValue();
                default -> false;
            };
            if (result) score += currentSeedRule.getScore();
        }

        if (reader.readInt() != 0x87654321) log.error("错误的文件格式 - 结束头丢失");

        return new Seed(attempt, seed1, seed2, seed3, enemies.reversed(), score);
    }
}
