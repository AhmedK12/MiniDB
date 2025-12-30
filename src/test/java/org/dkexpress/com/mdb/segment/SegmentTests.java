package org.dkexpress.com.mdb.segment;

import org.dkexpress.com.mdb.service.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.util.AssertionErrors.assertEquals;

public class SegmentTests {


    @Test
    void writeAndReadSingleSegment() throws IOException {
        SegmentManagerService segmentManagerService = new SegmentManagerService(100, "segments");
        StartupRecovery startupRecovery = new StartupRecovery();
        LogWriter logWriter = new LogWriter();
        LogReader logReader = new LogReader();
        MiniDB store = new MiniDB(segmentManagerService,logWriter,logReader,startupRecovery);
        store.dbSet("a", "1");
        store.dbSet("b", "2");

        assertEquals("","1", store.dbGet("a"));
        assertEquals("","2", store.dbGet("b"));
    }

    @Test
    void segmentRotatesWhenSizeExceeded() throws IOException {
        // configure very small segment size for test
        SegmentManagerService segmentManagerService = new SegmentManagerService(100, "segments");
        StartupRecovery startupRecovery = new StartupRecovery();
        LogWriter logWriter = new LogWriter();
        LogReader logReader = new LogReader();
        MiniDB store = new MiniDB(segmentManagerService,logWriter,logReader,startupRecovery);

        for (int i = 0; i < 100; i++) {
            store.dbSet("key" + i, "value" + i);
        }
        assertTrue(segmentManagerService.getSegmentCount() > 1);
    }

    @Test
    void readAcrossSegments() throws IOException {
        SegmentManagerService segmentManagerService = new SegmentManagerService(100, "segments");
        StartupRecovery startupRecovery = new StartupRecovery();
        LogWriter logWriter = new LogWriter();
        LogReader logReader = new LogReader();
        MiniDB store = new MiniDB(segmentManagerService,logWriter,logReader,startupRecovery);

        store.dbSet("a", "1"); // segment 1
        store.dbSet("b", "2"); // segment 2 (after rotation)

        assertEquals("","1", store.dbGet("a"));
        assertEquals("","2", store.dbGet("b"));
    }

    @Test
    void restartRebuildsIndexCorrectly() throws IOException {
        SegmentManagerService segmentManagerService = new SegmentManagerService(100, "segments");
        StartupRecovery startupRecovery = new StartupRecovery();
        LogWriter logWriter = new LogWriter();
        LogReader logReader = new LogReader();
        MiniDB store = new MiniDB(segmentManagerService,logWriter,logReader,startupRecovery);
        store.dbSet("a", "1");
        store.dbSet("b", "2");

        store.close(); // simulate shutdown


         store = new MiniDB(segmentManagerService,logWriter,logReader,startupRecovery);

        assertEquals("","1", store.dbGet("a"));
        assertEquals("","2", store.dbGet("b"));
    }
}
