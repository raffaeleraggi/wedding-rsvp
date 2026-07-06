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
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
                .status(RsvpStatus.IN_ATTESA)
                .shortCode(generateShortCode())
                .build();

        return toAdminDto(guestRepository.save(guest));
    }

    @Transactional(readOnly = true)
    public GuestInviteDto findInvite(String token) {
        GuestEntity guest = findByShortCode(token);

        return GuestInviteDto.builder()
                .name(guest.getName())
                .surname(guest.getSurname())
                .status(guest.getStatus())
                .additionalAdults(resolveAdditionalAdults(guest))
                .childrenCount(resolveChildrenCount(guest))
                .childrenAges(guest.getChildrenAges())
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
        GuestEntity guest = findByShortCode(token);

        int additionalAdults = nonNegative(
                request.getAdditionalAdults() != null
                        ? request.getAdditionalAdults()
                        : 0);

        int childrenCount = nonNegative(request.getChildrenCount());

        guest.setStatus(request.getStatus());

        guest.setAdditionalAdults(additionalAdults);
        guest.setChildrenCount(childrenCount);
        guest.setChildrenAges(cleanChildrenAges(request.getChildrenAges(), childrenCount));
        guest.setAllergies(request.getAllergies());
        guest.setMessage(request.getMessage());
        guest.setRepliedAt(LocalDateTime.now());

        GuestEntity saved = guestRepository.save(guest);

        return GuestInviteDto.builder()
                .name(saved.getName())
                .surname(saved.getSurname())
                .status(saved.getStatus())
                .additionalAdults(saved.getAdditionalAdults())
                .childrenCount(saved.getChildrenCount())
                .childrenAges(saved.getChildrenAges())
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

    public GuestEntity findByShortCode(String codice) {
        return guestRepository.findByShortCode(codice)
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
                .additionalPeople(totalAdditionalPeople(guest))
                .additionalAdults(resolveAdditionalAdults(guest))
                .childrenCount(resolveChildrenCount(guest))
                .childrenAges(guest.getChildrenAges())
                .allergies(guest.getAllergies())
                .message(guest.getMessage())
                .whatsappSent(guest.getWhatsappSent())
                .build();
    }

    private String buildInviteUrl(GuestEntity guest) {
        return frontendBaseUrl + "/invitati/" + guest.getShortCode();
    }

    private String buildWhatsappLink(GuestEntity guest, String inviteUrl) {
        String phone = guest.getPhone() == null ? "" : guest.getPhone().replaceAll("[^0-9]", "");
        String message = """
            Ciao  %s 💙

            Siamo felici di invitarti al nostro matrimonio.

            Apri la tua partecipazione qui:
            %s
            """.formatted(guest.getName(), inviteUrl);
        return "https://wa.me/" + phone + "?text=" + URLEncoder.encode(message, StandardCharsets.UTF_8);
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

    private String generateShortCode() {

        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        SecureRandom random = new SecureRandom();

        String code;

        do {

            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < 6; i++) {
                sb.append(chars.charAt(random.nextInt(chars.length())));
            }

            code = sb.toString();

        } while (guestRepository.existsByShortCode(code));

        return code;
    }

    @Transactional
    public void populateShortCodes() {

        List<GuestEntity> guests = guestRepository.findAll();

        for (GuestEntity guest : guests) {
            if (guest.getShortCode() == null) {
                guest.setShortCode(generateShortCode());
            }
        }

        guestRepository.saveAll(guests);
    }

    public List<GuestBackupDto> backupGuests() {
        return guestRepository.findAll()
                .stream()
                .map(this::toBackupDto)
                .toList();
    }

    @Transactional
    public void restoreGuests(List<GuestBackupDto> guests) {
        for (GuestBackupDto dto : guests) {

            if (dto.getShortCode() != null &&
                    guestRepository.existsByShortCode(dto.getShortCode())) {
                continue;
            }

            GuestEntity guest = GuestEntity.builder()
                    .name(dto.getName())
                    .surname(dto.getSurname())
                    .email(dto.getEmail())
                    .phone(dto.getPhone())
                    .token(dto.getToken() != null ? dto.getToken() : UUID.randomUUID().toString())
                    .shortCode(dto.getShortCode() != null ? dto.getShortCode() : generateShortCode())
                    .whatsappSent(Boolean.TRUE.equals(dto.getWhatsappSent()))
                    .whatsappSentAt(dto.getWhatsappSentAt())
                    .status(dto.getStatus() != null ? dto.getStatus() : RsvpStatus.IN_ATTESA)
                    .additionalAdults(
                            dto.getAdditionalAdults() != null
                                    ? dto.getAdditionalAdults()
                                    : 0
                    )
                    .childrenCount(dto.getChildrenCount() != null ? dto.getChildrenCount() : 0)
                    .childrenAges(dto.getChildrenAges())
                    .allergies(dto.getAllergies())
                    .message(dto.getMessage())
                    .repliedAt(dto.getRepliedAt())
                    .build();

            guestRepository.save(guest);
        }
    }

    private GuestBackupDto toBackupDto(GuestEntity guest) {
        GuestBackupDto dto = new GuestBackupDto();

        dto.setName(guest.getName());
        dto.setSurname(guest.getSurname());
        dto.setEmail(guest.getEmail());
        dto.setPhone(guest.getPhone());
        dto.setToken(guest.getToken());
        dto.setShortCode(guest.getShortCode());
        dto.setWhatsappSentAt(guest.getWhatsappSentAt());
        dto.setStatus(guest.getStatus());
        dto.setAdditionalAdults(guest.getAdditionalAdults());
        dto.setChildrenCount(guest.getChildrenCount());
        dto.setChildrenAges(guest.getChildrenAges());
        dto.setAllergies(guest.getAllergies());
        dto.setMessage(guest.getMessage());
        dto.setRepliedAt(guest.getRepliedAt());

        return dto;
    }

    private int resolveAdditionalAdults(GuestEntity guest) {
        return nonNegative(
                guest.getAdditionalAdults() != null
                        ? guest.getAdditionalAdults()
                        : 0
        );
    }

    private int resolveChildrenCount(GuestEntity guest) {
        return nonNegative(guest.getChildrenCount());
    }

    private int totalAdditionalPeople(GuestEntity guest) {
        return resolveAdditionalAdults(guest) + resolveChildrenCount(guest);
    }

    private int nonNegative(Integer value) {
        return value == null || value < 0 ? 0 : value;
    }

    private String cleanChildrenAges(String childrenAges, int childrenCount) {
        if (childrenCount <= 0 || childrenAges == null || childrenAges.isBlank()) {
            return null;
        }

        return childrenAges.trim();
    }

}
