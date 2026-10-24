package com.skillswap.controller;

import com.skillswap.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/files")
@Tag(name = "Files")
public class FileController {

    private final FileStorageService storage;

    public FileController(FileStorageService storage) {
        this.storage = storage;
    }

    @GetMapping("/avatars/{name}")
    @Operation(summary = "Download a profile photo")
    public ResponseEntity<Resource> avatar(@PathVariable String name) {
        Resource resource = storage.loadAvatar(name);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(FileStorageService.contentTypeOf(name)))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(1)).cachePublic())
                .header("X-Content-Type-Options", "nosniff")
                .body(resource);
    }
}
