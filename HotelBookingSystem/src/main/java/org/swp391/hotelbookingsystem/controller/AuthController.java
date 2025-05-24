package org.swp391.hotelbookingsystem.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;

@Controller
public class AuthController {

    @GetMapping("/sign-in")
    public String showSignInForm() {
        return "page/sign-in";
    }


    @GetMapping("/sign-up")
    public String showSignUpForm() {
        return "page/sign-up";
    }

}