package org.wedding.rsvp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.wedding.rsvp.dto.CreateGuestRequest;
import org.wedding.rsvp.dto.DashboardStatsDto;
import org.wedding.rsvp.dto.GuestAdminDto;
import org.wedding.rsvp.dto.GuestBackupDto;
import org.wedding.rsvp.repository.GuestRepository;
import org.wedding.rsvp.service.GuestPdfService;
import org.wedding.rsvp.service.GuestService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final GuestService guestService;
    private final GuestPdfService guestPdfService;

    @GetMapping("/guests")
    public List<GuestAdminDto> findAll() {
        return guestService.findAll();
    }

    @PostMapping("/guests")
    public GuestAdminDto create(@Valid @RequestBody CreateGuestRequest request) {
        return guestService.create(request);
    }

    @PostMapping("/guests/import-csv")
    public Map<String, Integer> importCsv(@RequestPart("file") MultipartFile file) {
        return Map.of("imported", guestService.importCsv(file));
    }

    @GetMapping("/stats")
    public DashboardStatsDto stats() {
        return guestService.stats();
    }

    @PostMapping("/guests/{id}/send-whatsapp")
    public GuestAdminDto sendWhatsapp(@PathVariable Long id) {
        return guestService.sendWhatsapp(id);
    }

    @PostMapping("/guests/send-whatsapp-all")
    public Map<String, Integer> sendWhatsappAll() {
        return Map.of("sent", guestService.sendWhatsappAllPending());
    }

    @GetMapping("/guests/export-pdf")
    public ResponseEntity<byte[]> exportGuestsPdf() {
        byte[] pdf = guestPdfService.exportGuestsPdf();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Lista Invitati.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @DeleteMapping("/guests/{id}")
    public ResponseEntity<Void> deleteGuest(@PathVariable Long id) {
        guestService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/guests/backup")
    public ResponseEntity<List<GuestBackupDto>> backupGuests() {
        return ResponseEntity.ok(guestService.backupGuests());
    }

    @PostMapping("/guests/restore")
    public ResponseEntity<Void> restoreGuests(@RequestBody List<GuestBackupDto> guests) {
        guestService.restoreGuests(guests);
        return ResponseEntity.ok().build();
    }

}
