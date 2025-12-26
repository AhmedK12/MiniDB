package org.dkexpress.com.mdb.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class MiniDBService {

    private static final String DB_FILE = "database.db";
    private static final Map<String, Long> index = new HashMap<>();

    // Load existing data and build index
    static {
        log.info("Loading database file :{}",DB_FILE);
        try (RandomAccessFile raf = new RandomAccessFile(DB_FILE, "rw")) {
            long pos = 0;
            String line;
            while ((line = raf.readLine()) != null) {
                int comma = line.indexOf(',');
                if (comma > 0) {
                    String key = line.substring(0, comma);
                    index.put(key, pos);
                    log.info("Loaded key {} from database", key);
                    log.info("Key offset :{}",pos);
                }
                pos = raf.getFilePointer();
            }
        } catch (FileNotFoundException e) {
            // file doesn't exist yet, ignore
        } catch (IOException e) {
            log.info("Error reading database file :{} :{}",DB_FILE, e.getMessage());
        }
    }

    public  void dbSet(String key, String value) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(DB_FILE, "rw")) {
            log.info("Saving key {} to database", key);
            raf.seek(raf.length());
            long offset = raf.getFilePointer();
            log.info("Key offset :{}",offset);
            raf.write((key + "," + value + "\n").getBytes(StandardCharsets.UTF_8));
            index.put(key, offset);
        }
    }

    public  String dbGet(String key) throws IOException {
        Long offset = index.get(key);
        if (offset == null) {
            log.info("Key {} not found in database", key);
            return "Not found";
        }
        log.info("Key offset :{}",offset);

        try (RandomAccessFile raf = new RandomAccessFile(DB_FILE, "r")) {
            raf.seek(offset);
            return raf.readLine().split(",", 2)[1];
        }
    }


}