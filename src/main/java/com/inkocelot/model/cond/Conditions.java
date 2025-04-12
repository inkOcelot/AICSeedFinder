package com.inkocelot.model.cond;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Conditions {
    private List<SeedCondition> seed;
    private List<EnemyCondition> enemy;
}
