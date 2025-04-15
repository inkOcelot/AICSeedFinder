package com.inkocelot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Enemy {
    private int order;
    private String enemyId;
    private int attr;
    private boolean overdrive;
    private int score;
    private List<Integer> seedMatches;
}
