package org.wedding.rsvp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontendController {

    @GetMapping(value = {
            "/",
            "/admin",
            "/invitati/{token}"
    })
    public String forward() {
        return "forward:/index.html";
    }
}