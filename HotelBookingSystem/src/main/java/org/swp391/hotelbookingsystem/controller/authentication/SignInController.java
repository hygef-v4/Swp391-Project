package org.swp391.hotelbookingsystem.controller.authentication;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SignInController {
    @GetMapping({"/sign-in"})
    public String home() {
        return "page/sign-in";
    }
}