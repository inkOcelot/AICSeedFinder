package com.inkocelot.model.cond;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnemyCondition {
    private String enemyId;
    private Boolean isOverdrive;
    private Integer attrs;
    private Integer order;
    private Integer score;
}
