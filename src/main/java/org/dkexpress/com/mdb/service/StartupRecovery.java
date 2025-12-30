package org.dkexpress.com.mdb.service;

import lombok.extern.slf4j.Slf4j;
import org.dkexpress.com.mdb.Index;
import org.dkexpress.com.mdb.Location;
import org.dkexpress.com.mdb.Segment;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;

@Slf4j
@Component
public class StartupRecovery {

    private void initializeOnStartup(SegmentManagerService segmentManagerService, Index index) throws IOException {
        log.info("Starting database initialization");
        Files.createDirectories(Path.of(segmentManagerService.SEGMENT_DIR));
        log.info("Created segment directory: {}", segmentManagerService.SEGMENT_DIR);

        File[] files = new File(segmentManagerService.SEGMENT_DIR)
                .listFiles((dir, name) -> name.startsWith(SegmentManagerService.SEGMENT_FILE_PREFIX) && name.endsWith(".log"));

        if (files == null || files.length == 0) {
            log.info("No existing segments found. Creating new segment");
            segmentManagerService.nextSegmentId = 1;
            segmentManagerService.activeSegment = segmentManagerService.createNewSegment();
            return;
        }

        Arrays.sort(files, Comparator.comparingInt(segmentManagerService::extractSegmentId));
        log.info("Found {} existing segments to scan", files.length);

        for (File file : files) {
            int id = segmentManagerService.extractSegmentId(file);
            scanSegment(id, file, index);
        }

        File last = files[files.length - 1];
        int lastId = segmentManagerService.extractSegmentId(last);

        segmentManagerService.activeSegment = new Segment(
                lastId,
                last,
                new RandomAccessFile(last, "rw")
        );
        log.info("Initialized active segment with ID: {}", lastId);

        segmentManagerService.nextSegmentId = lastId + 1;
    }


    private void scanSegment(int segmentId, File file, Index index) throws IOException {
        log.info("Initializing database from file: {}", file.getName());
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            long offset = 0;
            int recordCount = 0;
            while (true) {
                try {
                    raf.seek(offset);
                    int keyLen = raf.readInt();
                    int valLen = raf.readInt();
                    byte[] keyBytes = new byte[keyLen];
                    raf.readFully(keyBytes);
                    // skip value bytes
                    raf.skipBytes(valLen);
                    String key = new String(keyBytes, StandardCharsets.UTF_8);
                    index.put(key, new Location(segmentId, offset));
                    log.debug("Loaded key: {} at position: {}", key, offset);
                    recordCount++;
                    offset += SegmentManagerService.KEY_LENGTH_VALUE_LENGTH + keyLen + valLen;
                } catch (EOFException e) {
                    // Partial record at end → ignore safely
                    log.debug("Detected partial record at end of segment {}, ignoring", segmentId);
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

    public void recover(SegmentManagerService mgr, Index index) {
        try {
            initializeOnStartup(mgr, index);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }
}
