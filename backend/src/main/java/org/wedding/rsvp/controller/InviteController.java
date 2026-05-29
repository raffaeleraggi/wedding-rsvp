package org.wedding.rsvp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.wedding.rsvp.dto.GuestInviteDto;
import org.wedding.rsvp.dto.GuestReplyRequest;
import org.wedding.rsvp.service.GuestService;

@RestController
@RequestMapping("/api/invite")
@RequiredArgsConstructor
public class InviteController {

    private final GuestService guestService;

    @GetMapping("/{token}")
    public GuestInviteDto getInvite(@PathVariable String token) {
        return guestService.findInvite(token);
    }

    @PostMapping("/{token}/reply")
    public GuestInviteDto reply(@PathVariable String token, @Valid @RequestBody GuestReplyRequest request) {
        return guestService.reply(token, request);
    }
}
