package com.inkocelot.model.mode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class SlidingWindowMappedConf extends Conf {
    private int threads;
    private int windowSize;
    private int chunkBuffer;
    private int buffer;
}
