package org.swp391.hotelbookingsystem.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class FormLoginSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UserRepo userRepo;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            User user = userRepo.findByEmail(userDetails.getUsername());
            if (user != null) {
                if (!user.isActive()) {
                    response.sendRedirect("/login?error=inactive");
                    return;
                } else {
                    request.getSession().setAttribute("user", user);
                    switch (user.getRole()) {
                        case "ADMIN", "MODERATOR" -> response.sendRedirect("/admin-dashboard");
                        case "HOTEL_OWNER" -> response.sendRedirect("/host-dashboard");
                        case "CUSTOMER" -> response.sendRedirect("/home");
                        default -> response.sendRedirect("/home");
                    }
                }
            } else {
                response.sendRedirect("/login?error=true");
                return;
            }
        }
    }
}
