package org.wedding.rsvp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.wedding.rsvp.entity.GuestEntity;
import org.wedding.rsvp.entity.RsvpStatus;

import java.util.Optional;

public interface GuestRepository extends JpaRepository<GuestEntity, Long> {

    Optional<GuestEntity> findByToken(String token);


    long countByStatus(RsvpStatus status);

    long countByWhatsappSentTrue();

    @Query("select coalesce(sum(coalesce(g.additionalGuests, 0) + 1), 0) from GuestEntity g where g.status = org.wedding.rsvp.entity.RsvpStatus.CONFERMATO")
    long sumConfirmedPeople();

    @Query("select coalesce(sum(g.additionalGuests), 0) from GuestEntity g where g.status = :status")
    long sumAdditionalGuestsByStatus(RsvpStatus status);

    boolean existsByShortCode(String shortCode);

    Optional<GuestEntity> findByShortCode(String shortCode);
}
