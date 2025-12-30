package org.dkexpress.com.mdb.service;

import lombok.extern.slf4j.Slf4j;
import org.dkexpress.com.mdb.Index;
import org.dkexpress.com.mdb.Location;
import org.dkexpress.com.mdb.Segment;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
@Slf4j
public class MiniDB implements AutoCloseable{
    private final SegmentManagerService segmentManagerService;
    private final LogWriter logWriter;
    private final LogReader logReader;
    private final Index index = new Index();
    private final Object writeLock = new Object();


   public MiniDB(SegmentManagerService segmentManagerService, LogWriter logWriter, LogReader logReader, StartupRecovery startupRecovery) throws IOException {
        this.segmentManagerService = segmentManagerService;
        this.logWriter = logWriter;
        this.logReader = logReader;
       startupRecovery.recover(segmentManagerService, index);

    }


    public void dbSet(String key, String value) throws IOException {
        log.info("Setting value for key: {}, value: {}", key, value);

        synchronized (writeLock) {
            long recordSize = logWriter.recordSize(key, value);
            segmentManagerService.maybeRotateSegment(recordSize);
            long offset = logWriter.append(segmentManagerService.activeSegment, key, value);
            Location location = new Location(segmentManagerService.activeSegment.getId(), offset);
            index.put(key, location);
            log.info("Successfully wrote value for key: {} at offset: {}", key, offset);
        }
    }

    public String dbGet(String key) throws IOException {
        Location location = index.get(key);
        if (location == null) {
            return null;
        }

        Segment segment = segmentManagerService.getSegment(location.getSegmentId());
        return logReader.read(segment, location.getOffset());
    }

    public void close() throws IOException {
        synchronized (writeLock) {
            if (segmentManagerService.activeSegment != null) {
                segmentManagerService.activeSegment.getRaf().close();
            }
        }
    }

}