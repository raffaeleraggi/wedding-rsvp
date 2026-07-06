package org.wedding.rsvp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "guest")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String surname;
    private String email;
    private String phone;

    @Column(unique = true, nullable = false)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RsvpStatus status;

    @Column(name = "additional_adults")
    private Integer additionalAdults;

    @Column(name = "children_count")
    private Integer childrenCount;

    @Column(name = "children_ages", length = 255)
    private String childrenAges;

    @Column(length = 1000)
    private String allergies;

    @Column(length = 2000)
    private String message;

    private Boolean whatsappSent;

    private LocalDateTime createdAt;
    private LocalDateTime repliedAt;
    private LocalDateTime whatsappSentAt;

    @Column(length = 12)
    private String shortCode;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();

        if (this.token == null || this.token.isBlank()) {
            this.token = UUID.randomUUID().toString();
        }

        if (this.status == null) {
            this.status = RsvpStatus.IN_ATTESA;
        }

        if (this.whatsappSent == null) {
            this.whatsappSent = false;
        }

        if (this.additionalAdults == null) {
            this.additionalAdults = 0;
        }

        if (this.childrenCount == null) {
            this.childrenCount = 0;
        }
    }
}
