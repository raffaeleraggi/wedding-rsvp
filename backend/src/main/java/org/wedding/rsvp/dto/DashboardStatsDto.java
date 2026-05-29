package org.wedding.rsvp.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardStatsDto {
    private long totalGuests;
    private long pendingGuests;
    private long confirmedGuests;
    private long declinedGuests;
    private long totalConfirmedPeople;
    private long whatsappSentCount;
}
