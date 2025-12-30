package org.dkexpress.com.mdb.controller;

import lombok.extern.slf4j.Slf4j;
import org.dkexpress.com.mdb.service.MiniDB;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Slf4j
@RestController()
@RequestMapping("/api/v1/db")
public class ReadWriteController {
    private final MiniDB miniDB;
    public ReadWriteController(MiniDB miniDB) {
        this.miniDB = miniDB;
    }

    @PostMapping("/write")
    public void writeValue(@RequestParam String key, @RequestParam String value) throws IOException {
        miniDB.dbSet(key, value);
    }

    @GetMapping("/read")
    public String readValue(@RequestParam String key) throws IOException {
        return miniDB.dbGet(key);
    }

}
