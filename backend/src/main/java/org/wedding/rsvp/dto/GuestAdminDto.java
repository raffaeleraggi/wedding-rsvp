package org.wedding.rsvp.dto;

import lombok.Builder;
import lombok.Data;
import org.wedding.rsvp.entity.RsvpStatus;

@Data
@Builder
public class GuestAdminDto {
    private Long id;
    private String name;
    private String surname;
    private String email;
    private String phone;
    private String token;
    private String inviteUrl;
    private String whatsappLink;
    private RsvpStatus status;
    private Integer additionalPeople;
    private Integer additionalAdults;
    private Integer childrenCount;
    private String childrenAges;
    private String allergies;
    private String message;
    private Boolean whatsappSent;
}
