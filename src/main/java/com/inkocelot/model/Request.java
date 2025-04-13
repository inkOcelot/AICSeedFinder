package com.inkocelot.model;

import com.inkocelot.model.cond.Conditions;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Request {
    private String type = "local";
    private String path;
    private Boolean isParallel;
    private Integer parallelNumber;
    private Integer slidingWindowSize;
    private int buffer = 64;
    private int size = 20;
    private Conditions cond;
}
