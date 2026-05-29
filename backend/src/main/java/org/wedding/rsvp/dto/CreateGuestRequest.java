package org.wedding.rsvp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateGuestRequest {
    @NotBlank
    private String name;

    private String surname;
    private String email;
    private String phone;
    private Integer additionalGuests;
}
