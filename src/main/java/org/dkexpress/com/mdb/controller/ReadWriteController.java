package org.dkexpress.com.mdb.controller;

import lombok.extern.slf4j.Slf4j;
import org.dkexpress.com.mdb.service.MiniDBService;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Slf4j
@RestController()
@RequestMapping("/api/v1/db")
public class ReadWriteController {
    private final MiniDBService miniDBService;
    public ReadWriteController(MiniDBService miniDBService, MiniDBService miniDBService1) {
        this.miniDBService = miniDBService1;
    }

    @PostMapping("/write")
    public void writeValue(@RequestParam String key, @RequestParam String value) throws IOException {
        miniDBService.dbSet(key, value);
    }

    @GetMapping("/read")
    public String readValue(@RequestParam String key) throws IOException {
        return miniDBService.dbGet(key);
    }

}
