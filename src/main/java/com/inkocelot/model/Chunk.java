package com.inkocelot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Chunk {
    private long start;
    private long end;

    @Override
    public String toString() {
        String startHexString = "0x" + String.format("%08X", start);
        String endHexString = "0x" + String.format("%08X", end);
        var size = end - start;
        return String.format(
                "Chunk(StartAt: %s, EndAt: %s, Size: %s)",
                startHexString, endHexString, size
        );
    }
}
