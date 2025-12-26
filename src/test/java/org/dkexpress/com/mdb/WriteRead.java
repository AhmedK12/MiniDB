package org.dkexpress.com.mdb;

import org.dkexpress.com.mdb.service.MiniDBService;
import org.junit.jupiter.api.*;

import java.io.IOException;

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
}