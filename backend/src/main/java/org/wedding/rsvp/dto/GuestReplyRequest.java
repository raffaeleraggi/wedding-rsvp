package org.wedding.rsvp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.wedding.rsvp.entity.RsvpStatus;

@Data
public class GuestReplyRequest {
    @NotNull
    private RsvpStatus status;

    private Integer additionalGuests;
    private String allergies;
    private String message;
}
