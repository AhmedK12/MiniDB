package org.dkexpress.com.mdb;

import org.dkexpress.com.mdb.service.MiniDBService;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.RandomAccessFile;

import static org.junit.jupiter.api.Assertions.*;

class KeyValueStoreTest {

    MiniDBService store;

    @BeforeEach
    void setup() {
//        MdbApplicationTests.deleteDbFile();
        store = new MiniDBService();
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

        store = new MiniDBService(); // restart
        assertEquals("1", store.dbGet("a"));
    }
}