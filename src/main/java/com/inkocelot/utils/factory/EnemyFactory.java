package com.inkocelot.utils.factory;

import com.inkocelot.model.Enemy;
import com.inkocelot.model.cond.EnemyCondition;
import com.inkocelot.utils.file.LittleEndianReader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class EnemyFactory {

    private static boolean isZero(int n) {
        return n == 0;
    }

    public static Enemy createFromFile(
            LittleEndianReader reader,
            List<EnemyCondition> conditions,
            int order
    ) throws IOException {
        var length = reader.readUnsignedByte();
        var bytes = new byte[length];
        reader.readFully(bytes);
        var enemyId = new String(bytes, StandardCharsets.UTF_8).substring(0, length - 2);
        var attr = reader.readInt();
        var overdrive = reader.readByte() != 0;

        var score = 0;
        for (EnemyCondition cond : conditions) {
            // 判断位置
            if (cond.getOrder() != null) {
                if (order != cond.getOrder()) {
                    continue;
                }
            }

            // 判断怪物种类
            if (cond.getEnemyId() != null) {
                if (!enemyId.equals(cond.getEnemyId())) {
                    continue;
                }
            }

            // 判断是否为污染体
            if (cond.getIsOverride() != null) {
                if (overdrive != cond.getIsOverride()) {
                    continue;
                }
            }

            // 判断属性是否匹配
            if (cond.getAttrs() != null) {
                if (cond.getAttrs().stream().anyMatch(n -> isZero(attr & n))) {
                    continue;
                }
            }

            score += cond.getScore();
        }

        return new Enemy(enemyId, attr, overdrive, score);
    }
}
