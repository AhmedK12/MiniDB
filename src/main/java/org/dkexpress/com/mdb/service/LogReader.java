package org.dkexpress.com.mdb.service;

import lombok.extern.slf4j.Slf4j;
import org.dkexpress.com.mdb.Location;
import org.dkexpress.com.mdb.Segment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class LogReader {

    public String read(Segment segment, long offset) throws IOException {

        try (RandomAccessFile raf = new RandomAccessFile(segment.getFile(), "r")) {
            raf.seek(offset);
            int keyLen = raf.readInt();
            int valLen = raf.readInt();
            byte[] keyBytes = new byte[keyLen];
            byte[] valBytes = new byte[valLen];
            raf.readFully(keyBytes);
            raf.readFully(valBytes);
            return new String(valBytes, StandardCharsets.UTF_8);
        }
    }
}
