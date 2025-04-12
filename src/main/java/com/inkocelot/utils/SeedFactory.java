package com.inkocelot.utils;

import com.inkocelot.model.Enemy;
import com.inkocelot.model.Seed;
import com.inkocelot.model.cond.Conditions;
import com.inkocelot.model.cond.SeedCondition;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

@Slf4j
public class SeedFactory {

    public static Seed createFromFile(LittleEndianDataReader reader, Conditions conditions) throws IOException {
        if (reader.readInt() != 0x12345678) log.error("错误的文件格式 - 起始头丢失");

        var attempt = reader.readInt();
        var seed1 = new int[4];
        var seed2 = new int[4];
        var seed3 = new int[4];
        var enemies = new ArrayList<Enemy>();

        for (int i = 0; i < 4; i++) seed1[i] = reader.readInt();
        for (int i = 0; i < 4; i++) seed2[i] = reader.readInt();
        for (int i = 0; i < 4; i++) seed3[i] = reader.readInt();

        var count = reader.readInt();
        var score = 0;
        var enemyCount = new HashMap<String ,Integer>();

        // Enemy计分 从1开始数
        for (int i = 0; i < count; i++) {
            var enemy = EnemyFactory.createFromFile(reader, conditions.getEnemy(), count - i);
            if (enemyCount.containsKey(enemy.getEnemyId())) {
                enemyCount.put(enemy.getEnemyId(), enemyCount.get(enemy.getEnemyId()) + 1);
            } else {
                enemyCount.put(enemy.getEnemyId(), 1);
            }
            score += enemy.getScore();
            enemies.add(enemy);
        }

        // Seed计分
        for (SeedCondition sc : conditions.getSeed()) {
            var nowCount = enemyCount.get(sc.getEnemyId());
            if (nowCount == null) {
                continue;
            }
            var result = switch (sc.getOperator()) {
                case "eq" -> nowCount == sc.getValue();
                case "neq" -> nowCount != sc.getValue();
                case "lt" -> nowCount < sc.getValue();
                case "lte" -> nowCount <= sc.getValue();
                case "gt" -> nowCount > sc.getValue();
                case "gte" -> nowCount >= sc.getValue();
                default -> false;
            };
            if (result) score += sc.getScore();
        }


        if (reader.readInt() != 0x87654321) log.error("错误的文件格式 - 结束头丢失");

        return new Seed(attempt, seed1, seed2, seed3, enemies, score);
    }
}
