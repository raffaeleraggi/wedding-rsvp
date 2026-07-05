package org.wedding.rsvp;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.wedding.rsvp.service.GuestService;

@SpringBootApplication
@EnableScheduling
public class WeddingRsvpApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeddingRsvpApplication.class, args);
    }
}
