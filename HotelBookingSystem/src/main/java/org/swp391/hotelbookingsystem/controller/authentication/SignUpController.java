package org.swp391.hotelbookingsystem.controller.authentication;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SignUpController {
    @GetMapping({"/sign-up"})
    public String home() {
        return "page/sign-up";
    }
}