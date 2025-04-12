package com.inkocelot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Enemy {
    private String enemyId;
    private int attr;
    private boolean overdrive;
    private int score;
}
