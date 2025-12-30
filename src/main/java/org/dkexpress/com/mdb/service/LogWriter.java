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
public class LogWriter {

    private static final long KEY_LENGTH_VALUE_LENGTH = 8L;

    public long append(Segment segment, String key, String value) throws IOException {
        log.info("Setting value for key: {}, value: {}", key, value);
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] valueBytes = value.getBytes(StandardCharsets.UTF_8);
        try (RandomAccessFile raf = new RandomAccessFile(segment.getFile(), "rw")) {
            raf.seek(raf.length());
            long offset = raf.getFilePointer();
            raf.writeInt(keyBytes.length);
            raf.writeInt(valueBytes.length);
            raf.write(keyBytes);
            raf.write(valueBytes);
            segment.setSize(raf.length());
            log.info("Successfully wrote value for key: {} at offset: {}", key, offset);
            return offset;

        }

    }

    public long recordSize(String key, String value) {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] valueBytes = value.getBytes(StandardCharsets.UTF_8);
        return KEY_LENGTH_VALUE_LENGTH + keyBytes.length + valueBytes.length;
    }


}
