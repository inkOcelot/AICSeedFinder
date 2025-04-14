package com.inkocelot.model;

import com.inkocelot.model.cond.Conditions;
import com.inkocelot.model.mode.Conf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Request {
    private String type = "local";
    private String path;
    private Conf conf;
    private int size = 20;
    private Conditions cond;
}
