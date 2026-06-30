package org.wedding.rsvp.dto;

import lombok.Data;
import org.wedding.rsvp.entity.RsvpStatus;

import java.time.LocalDateTime;

@Data
public class GuestBackupDto {
    private String name;
    private String surname;
    private String email;
    private String phone;
    private String token;
    private String shortCode;
    private String inviteUrl;
    private String whatsappLink;
    private Boolean whatsappSent;
    private LocalDateTime whatsappSentAt;
    private RsvpStatus status;
    private Integer additionalGuests;
    private String allergies;
    private String message;
    private LocalDateTime repliedAt;
}