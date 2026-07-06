package org.wedding.rsvp.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.wedding.rsvp.service.GuestService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class BackupScheduler {

    private final GuestService guestService;
    private final ObjectMapper objectMapper;

    @Value("${app.backup.path:./backups}")
    private String backupPath;

    @Scheduled(cron = "0 0 5 * * *")
    public void backupGuestsEveryNight() throws Exception {
        Files.createDirectories(Path.of(backupPath));

        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"));

        Path file = Path.of(backupPath, "backup-invitati-" + timestamp + ".json");

        var guests = guestService.backupGuests();

        objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(file.toFile(), guests);
    }
}
