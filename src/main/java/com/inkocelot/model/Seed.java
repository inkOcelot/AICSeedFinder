package com.inkocelot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Seed {
    private int attempt;
    private long[] seed1;
    private long[] seed2;
    private long[] seed3;
    private List<Enemy> enemies;
    private int score;
}
