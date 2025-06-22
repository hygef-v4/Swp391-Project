package org.swp391.hotelbookingsystem.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.repository.UserRepo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class FormLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepo userRepo;

    public FormLoginSuccessHandler(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            User user = userRepo.findByEmail(userDetails.getUsername());
            if (user != null) {
                if (!user.isActive()) {
                    response.sendRedirect("/login?error=inactive");
                } else {
                    request.getSession().setAttribute("user", user);
                    switch (user.getRole()) {
                        case "ADMIN" -> response.sendRedirect("/admin-dashboard");
                        case "MODERATOR" -> response.sendRedirect("/moderator-dashboard");
                        case "HOTEL OWNER" -> response.sendRedirect("/host-dashboard");
                        default -> response.sendRedirect("/home");
                    }
                }
            } else {
                response.sendRedirect("/login?error=true");
            }
        }
    }
}
