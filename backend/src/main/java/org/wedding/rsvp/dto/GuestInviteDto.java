package org.wedding.rsvp.dto;

import lombok.Builder;
import lombok.Data;
import org.wedding.rsvp.entity.RsvpStatus;

@Data
@Builder
public class GuestInviteDto {
    private String name;
    private String surname;
    private RsvpStatus status;
    private Integer additionalAdults;
    private Integer childrenCount;
    private String childrenAges;
    private String allergies;
    private String message;
}
