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
    private int[] seed1 = new int[4];
    private int[] seed2 = new int[4];
    private int[] seed3 = new int[4];
    private List<Enemy> enemies = new ArrayList<>();
    private int score;
}
