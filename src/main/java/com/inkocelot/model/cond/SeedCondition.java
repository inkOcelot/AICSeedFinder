package com.inkocelot.model.cond;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeedCondition {
    private String enemyId;
    private String operator;
    private int value;
    private int score;
}
