package org.dkexpress.com.mdb.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class MiniDBService {

    private static final String DB_FILE = "database.db";
    private static final Map<String, Long> index = new ConcurrentHashMap<>();
    public static final int KEY_LENGTH_VALUE_LENGTH = 8;
    private final Object writeLock = new Object();


    static {
        log.info("Initializing database from file: {}", DB_FILE);
        try (RandomAccessFile raf = new RandomAccessFile(DB_FILE, "r")) {
            long pos = 0;
            int recordCount = 0;
            while (true) {
                try {
                    raf.seek(pos);
                    int keyLen = raf.readInt();
                    int valLen = raf.readInt();
                    byte[] keyBytes = new byte[keyLen];
                    raf.readFully(keyBytes);
                    // skip value bytes
                    raf.skipBytes(valLen);
                    String key = new String(keyBytes, StandardCharsets.UTF_8);
                    index.put(key, pos);
                    log.debug("Loaded key: {} at position: {}", key, pos);
                    recordCount++;
                    pos += KEY_LENGTH_VALUE_LENGTH + keyLen + valLen;
                } catch (EOFException e) {
                    // Partial record at end → ignore safely
                    break;
                }
            }
            log.info("Successfully loaded {} records from database", recordCount);
        } catch (FileNotFoundException e) {
            log.info("Database file not found. Starting with empty database.");
        } catch (IOException e) {
            log.error("Error reading database file: {}", e.getMessage());
            throw new RuntimeException("Failed to initialize database", e);
        }
    }
    
    

    public void dbSet(String key, String value) throws IOException {
        log.info("Setting value for key: {}, value: {}", key, value);
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] valueBytes = value.getBytes(StandardCharsets.UTF_8);

        synchronized (writeLock) {
            try (RandomAccessFile raf = new RandomAccessFile(DB_FILE, "rw")) {
                raf.seek(raf.length());
                long offset = raf.getFilePointer();

                raf.writeInt(keyBytes.length);
                raf.writeInt(valueBytes.length);
                raf.write(keyBytes);
                raf.write(valueBytes);

                index.put(key, offset);
                log.info("Successfully wrote value for key: {} at offset: {}", key, offset);
            }
        }
    }

    public String dbGet(String key) throws IOException {
        log.info("Getting value for key: {}", key);
        Long offset = index.get(key);
        if (offset == null) {
            log.info("Key not found: {}", key);
            return null;
        }
        try (RandomAccessFile raf = new RandomAccessFile(DB_FILE, "r")) {
            raf.seek(offset);
            int keyLen = raf.readInt();
            int valLen = raf.readInt();
            byte[] keyBytes = new byte[keyLen];
            byte[] valBytes = new byte[valLen];
            raf.readFully(keyBytes);
            raf.readFully(valBytes);
            String value = new String(valBytes, StandardCharsets.UTF_8);
            log.info("Successfully read value for key: {}", key);
            return value;
        }
    }


}