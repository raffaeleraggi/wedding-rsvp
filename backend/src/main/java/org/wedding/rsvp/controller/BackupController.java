package org.wedding.rsvp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/admin/backups")
@RequiredArgsConstructor
public class BackupController {

    @Value("${app.backup.path:./backups}")
    private String backupPath;

    @GetMapping
    public List<String> listBackups() throws Exception {
        Path dir = Path.of(backupPath);

        if (!Files.exists(dir)) {
            return List.of();
        }

        try (var stream = Files.list(dir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .sorted(Comparator.comparing(path -> path.toFile().lastModified(), Comparator.reverseOrder()))
                    .map(path -> path.getFileName().toString())
                    .toList();
        }
    }

    @GetMapping("/{filename}")
    public ResponseEntity<Resource> downloadBackup(@PathVariable String filename) {
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            return ResponseEntity.badRequest().build();
        }

        Path file = Path.of(backupPath, filename);

        if (!Files.exists(file)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }
}
