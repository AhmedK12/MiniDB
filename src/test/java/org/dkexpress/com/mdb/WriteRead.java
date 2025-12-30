package org.dkexpress.com.mdb;

import org.dkexpress.com.mdb.service.*;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.RandomAccessFile;

import static org.junit.jupiter.api.Assertions.*;

class ReadWriteTests {

    MiniDB store;

    @BeforeEach
    void setup() throws IOException {
//        MdbApplicationTests.deleteDbFile();
        SegmentManagerService segmentManagerService = new SegmentManagerService(100, "segments");
        StartupRecovery startupRecovery = new StartupRecovery();
        LogWriter logWriter = new LogWriter();
        LogReader logReader = new LogReader();
        MiniDB store = new MiniDB(segmentManagerService,logWriter,logReader,startupRecovery);
    }

    @Test
    void testSimplePutGet() throws IOException {
        store.dbSet("name", "Ali");
        store.dbSet("city", "Patna");
        assertEquals("Ali", store.dbGet("name"));
        assertEquals("Patna", store.dbGet("city"));
    }

    @Test
    void testPartialRecordIgnoredOnRestart() throws Exception {
        store.dbSet("a", "1");

        // simulate crash corruption
        try (RandomAccessFile raf = new RandomAccessFile("database.db", "rw")) {
            raf.seek(raf.length());
            raf.writeInt(10); // incomplete header
        }

        SegmentManagerService segmentManagerService = new SegmentManagerService(100, "segments");
        StartupRecovery startupRecovery = new StartupRecovery();
        LogWriter logWriter = new LogWriter();
        LogReader logReader = new LogReader();
        MiniDB store = new MiniDB(segmentManagerService,logWriter,logReader,startupRecovery);
        assertEquals("1", store.dbGet("a"));
    }
}