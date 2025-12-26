package org.dkexpress.com.mdb;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

@SpringBootTest
class MdbApplicationTests {

    @Test
    void contextLoads() {
    }

    public static void deleteDbFile() {
        File file = new File("database.db");
        if (file.exists()) {
            file.delete();
        }
    }

}
