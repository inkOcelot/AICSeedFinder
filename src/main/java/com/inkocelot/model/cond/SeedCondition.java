package com.inkocelot.model.cond;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeedCondition {
    private EnemyCondition enemy;
    private String operator;
    private int value;
    private int score;
}
