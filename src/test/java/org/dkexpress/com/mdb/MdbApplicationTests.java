package org.dkexpress.com.mdb;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

@SpringBootTest
class MdbApplicationTests {

    @Test
    void contextLoads() {
    }

    @BeforeEach
    public  void deleteDbFile() {
        File file = new File("database.db");
        if (file.exists()) {
            file.delete();
        }
    }

}
