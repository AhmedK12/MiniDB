package org.dkexpress.com.mdb;

import lombok.Data;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

@Data
public class Segment {
    private int id;
    private long size;
    private File file;
    private RandomAccessFile raf;

    public Segment(int id, File file, RandomAccessFile raf) throws IOException {
        this.id = id;
        this.file = file;
        this.raf = raf;
        this.size = raf.length();
    }
}
