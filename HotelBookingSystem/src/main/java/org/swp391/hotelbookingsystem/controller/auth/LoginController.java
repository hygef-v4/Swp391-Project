package org.swp391.hotelbookingsystem.controller.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {
    @GetMapping({"/login"})
    public String login() {
        return "page/login";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        HttpSession session = request.getSession();
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/login";
    }
}