package org.dkexpress.com.mdb.service;

import lombok.extern.slf4j.Slf4j;
import org.dkexpress.com.mdb.Segment;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class SegmentManagerService {

    public  String SEGMENT_DIR = "segments";
    public static final int KEY_LENGTH_VALUE_LENGTH = 8;
    private long MAX_SEGMENT_SIZE = 1024 * 1024; // 1MB
    public static final String SEGMENT_FILE_PREFIX = "segment-";
    public Segment activeSegment;
    public final Map<Integer, Segment> segments = new HashMap<>();
    public int nextSegmentId = 1;



    public SegmentManagerService(long segmentSize, String storageDir) throws IOException {
        File dir = new File(SEGMENT_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        SEGMENT_DIR = storageDir;
        MAX_SEGMENT_SIZE = segmentSize;
        activeSegment = createNewSegment();
    }

    public void rotateSegment() throws IOException {
        log.info("Rotating segment {}", activeSegment.getId());
        activeSegment.getRaf().close();
        Segment newSegment = createNewSegment();
        activeSegment = newSegment;
        segments.put(newSegment.getId(), newSegment);
        log.info("Segment {} successfully rotated.", activeSegment.getId());
    }

    public Segment createNewSegment() throws IOException {
        int newSegmentId = nextSegmentId++;

        File file = new File(SEGMENT_DIR, segmentFileName(newSegmentId));
        RandomAccessFile raf = new RandomAccessFile(file, "rw");

        Segment segment = new Segment(newSegmentId, file, raf);
        segments.put(newSegmentId, segment);

        return segment;
    }

    void maybeRotateSegment(long recordSize) throws IOException {
        log.info("Checking if segment needs to be rotated. Current size: {}, required size: {}", activeSegment.getSize(), recordSize);
        if (activeSegment.getSize() + recordSize > MAX_SEGMENT_SIZE) {
            rotateSegment();
        }
        else {
            log.debug("Segment {} is not full yet. Not rotating.", activeSegment.getId());
        }
    }

    public int getSegmentCount() {
        log.info("Number of segments: {}", segments.size());
        return segments.size();
    }

    public int extractSegmentId(File last) {
        return Integer.parseInt(last.getName().replace(SEGMENT_FILE_PREFIX, "").replace(".log", ""));
    }

    private String segmentFileName(int id) {
        return String.format("segment-%05d.log", id);
    }

    public Segment getSegment(int segmentId) {
        return activeSegment;
    }
}
