package org.dkexpress.com.mdb;

import lombok.Data;

@Data
public class Location {
    private int segmentId;
    private long offset;

    public Location(int segmentId, long offset) {
        this.segmentId = segmentId;
        this.offset = offset;
    }
}
