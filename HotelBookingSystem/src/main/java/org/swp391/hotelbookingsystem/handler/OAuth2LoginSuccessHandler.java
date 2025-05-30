package org.swp391.hotelbookingsystem.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UserRepo userRepo;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");

        User user = userRepo.findByEmail(email);
        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setFullname(name);
            userRepo.saveUserFromGoogle(user);
        }

        if (!user.isActive()) {
            response.sendRedirect("/login?error=inactive");
            return;
        }

        request.getSession().setAttribute("user", user);

        switch (user.getRole()) {
            case "ADMIN", "MODERATOR" -> response.sendRedirect("/home");
            case "HOTEL OWNER" -> response.sendRedirect("/home");
            case "CUSTOMER", "GUEST" -> response.sendRedirect("/home");
            default -> response.sendRedirect("/home");
        }

    }
}

