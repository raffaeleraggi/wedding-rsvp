package org.wedding.rsvp;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.wedding.rsvp.service.GuestService;

@SpringBootApplication
public class WeddingRsvpApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeddingRsvpApplication.class, args);
    }
    @Bean
    CommandLineRunner init(GuestService guestService) {
        return args -> guestService.populateShortCodes();
    }
}
