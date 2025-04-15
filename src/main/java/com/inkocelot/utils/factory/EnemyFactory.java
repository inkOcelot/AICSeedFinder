package com.inkocelot.utils.factory;

import com.inkocelot.model.Enemy;
import com.inkocelot.model.cond.EnemyCondition;
import com.inkocelot.model.cond.SeedCondition;
import com.inkocelot.utils.file.LittleEndianReader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EnemyFactory {

    private static boolean isZero(int n) {
        return n == 0;
    }

    private static boolean isMatchCondition(
            int order,
            String enemyId,
            boolean overdrive,
            int attr,
            EnemyCondition cond
    ) {
        // 判断位置
        if (cond.getOrder() != null) {
            if (order != cond.getOrder()) {
                return false;
            }
        }

        // 判断怪物种类
        if (cond.getEnemyId() != null) {
            if (!enemyId.equals(cond.getEnemyId())) {
                return false;
            }
        }

        // 判断是否为污染体
        if (cond.getIsOverride() != null) {
            if (overdrive != cond.getIsOverride()) {
                return false;
            }
        }

        // 判断属性是否匹配
        if (cond.getAttrs() != null) {
            return cond.getAttrs().stream().noneMatch(n -> isZero(attr & n));
        }
        return true;
    }

    public static Enemy createFromFile(
            LittleEndianReader reader,
            Map<Integer, SeedCondition> seedConditions,
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
        // 判断Enemy规则
        for (var cond : conditions) {
            if (isMatchCondition(order, enemyId, overdrive, attr, cond)) {
                score += cond.getScore();
            }
        }

        // 判断Seed规则
        var seedMatches = new ArrayList<Integer>();
        for (var entry : seedConditions.entrySet()) {
            if (isMatchCondition(order, enemyId, overdrive, attr, entry.getValue().getEnemy())) {
                seedMatches.add(entry.getKey());
            }
        }

        return new Enemy(enemyId, attr, overdrive, score, seedMatches);
    }
}
