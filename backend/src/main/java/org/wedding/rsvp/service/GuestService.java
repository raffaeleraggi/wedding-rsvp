package org.wedding.rsvp.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.wedding.rsvp.dto.*;
import org.wedding.rsvp.entity.GuestEntity;
import org.wedding.rsvp.entity.RsvpStatus;
import org.wedding.rsvp.repository.GuestRepository;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GuestService {

    private final GuestRepository guestRepository;
    private final WhatsAppService whatsAppService;

    @Value("${app.frontend-base-url}")
    private String frontendBaseUrl;

    @Transactional(readOnly = true)
    public List<GuestAdminDto> findAll() {
        return guestRepository.findAll()
                .stream()
                .map(this::toAdminDto)
                .toList();
    }

    @Transactional
    public GuestAdminDto create(CreateGuestRequest request) {
        GuestEntity guest = GuestEntity.builder()
                .name(request.getName())
                .surname(request.getSurname())
                .email(request.getEmail())
                .phone(request.getPhone())
                .additionalGuests(request.getAdditionalGuests())
                .status(RsvpStatus.IN_ATTESA)
                .build();

        return toAdminDto(guestRepository.save(guest));
    }

    @Transactional(readOnly = true)
    public GuestInviteDto findInvite(String token) {
        GuestEntity guest = findByToken(token);

        return GuestInviteDto.builder()
                .name(guest.getName())
                .surname(guest.getSurname())
                .status(guest.getStatus())
                .additionalGuests(guest.getAdditionalGuests())
                .allergies(guest.getAllergies())
                .message(guest.getMessage())
                .build();
    }

    @Transactional
    public void delete(Long id) {
        if (!guestRepository.existsById(id)) {
            throw new IllegalArgumentException("Invitato non trovato");
        }

        guestRepository.deleteById(id);
    }

    @Transactional
    public GuestInviteDto reply(String token, GuestReplyRequest request) {
        GuestEntity guest = findByToken(token);

        guest.setStatus(request.getStatus());
        guest.setAdditionalGuests(request.getAdditionalGuests() == null ? 1 : request.getAdditionalGuests());
        guest.setAllergies(request.getAllergies());
        guest.setMessage(request.getMessage());
        guest.setRepliedAt(LocalDateTime.now());

        GuestEntity saved = guestRepository.save(guest);

        return GuestInviteDto.builder()
                .name(saved.getName())
                .surname(saved.getSurname())
                .status(saved.getStatus())
                .additionalGuests(saved.getAdditionalGuests())
                .allergies(saved.getAllergies())
                .message(saved.getMessage())
                .build();
    }

    @Transactional
    public int importCsv(MultipartFile file) {
        int imported = 0;

        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setTrim(true)
                    .build()
                    .parse(reader);

            for (CSVRecord record : records) {
                GuestEntity guest = GuestEntity.builder()
                        .name(get(record, "name"))
                        .surname(get(record, "surname"))
                        .email(get(record, "email"))
                        .phone(get(record, "phone"))
                        .additionalGuests(parseInteger(get(record, "numberOfPeople")))
                        .status(RsvpStatus.IN_ATTESA)
                        .build();

                guestRepository.save(guest);
                imported++;
            }

            return imported;
        } catch (Exception e) {
            throw new IllegalArgumentException("Errore import CSV: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public DashboardStatsDto stats() {
        return DashboardStatsDto.builder()
                .totalGuests(guestRepository.count())
                .pendingGuests(guestRepository.countByStatus(RsvpStatus.IN_ATTESA))
                .confirmedGuests(guestRepository.countByStatus(RsvpStatus.CONFERMATO))
                .declinedGuests(guestRepository.countByStatus(RsvpStatus.RIFIUTATO))
                .totalConfirmedPeople(guestRepository.sumConfirmedPeople())
                .whatsappSentCount(guestRepository.countByWhatsappSentTrue())
                .build();
    }

    @Transactional
    public GuestAdminDto sendWhatsapp(Long guestId) {
        GuestEntity guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new IllegalArgumentException("Invitato non trovato"));

        whatsAppService.sendInvite(guest, buildInviteUrl(guest));

        guest.setWhatsappSent(true);
        guest.setWhatsappSentAt(LocalDateTime.now());

        return toAdminDto(guestRepository.save(guest));
    }

    @Transactional
    public int sendWhatsappAllPending() {
        List<GuestEntity> guests = guestRepository.findAll()
                .stream()
                .filter(g -> g.getPhone() != null && !g.getPhone().isBlank())
                .filter(g -> !Boolean.TRUE.equals(g.getWhatsappSent()))
                .toList();

        for (GuestEntity guest : guests) {
            whatsAppService.sendInvite(guest, buildInviteUrl(guest));
            guest.setWhatsappSent(true);
            guest.setWhatsappSentAt(LocalDateTime.now());
            guestRepository.save(guest);
        }

        return guests.size();
    }

    private GuestEntity findByToken(String token) {
        return guestRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invito non trovato"));
    }

    private GuestAdminDto toAdminDto(GuestEntity guest) {
        String inviteUrl = buildInviteUrl(guest);

        return GuestAdminDto.builder()
                .id(guest.getId())
                .name(guest.getName())
                .surname(guest.getSurname())
                .email(guest.getEmail())
                .phone(guest.getPhone())
                .token(guest.getToken())
                .inviteUrl(inviteUrl)
                .whatsappLink(buildWhatsappLink(guest, inviteUrl))
                .status(guest.getStatus())
                .additionalPeople(guest.getAdditionalGuests())
                .allergies(guest.getAllergies())
                .message(guest.getMessage())
                .whatsappSent(guest.getWhatsappSent())
                .build();
    }

    private String buildInviteUrl(GuestEntity guest) {
        return frontendBaseUrl + "/i/" + guest.getToken();
    }

    private String buildWhatsappLink(GuestEntity guest, String inviteUrl) {
        String phone = guest.getPhone() == null ? "" : guest.getPhone().replaceAll("[^0-9]", "");
        String text = "Ciao " + guest.getName() + "! Martina e Riccardo sono felici di invitarti al loro matrimonio. Conferma qui la tua presenza: " + inviteUrl;
        return "https://wa.me/" + phone + "?text=" + URLEncoder.encode(text, StandardCharsets.UTF_8);
    }

    private String get(CSVRecord record, String column) {
        try {
            return record.isMapped(column) ? record.get(column) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return 1;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 1;
        }
    }

}
