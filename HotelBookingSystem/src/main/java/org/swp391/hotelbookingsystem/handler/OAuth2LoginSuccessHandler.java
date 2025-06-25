package org.swp391.hotelbookingsystem.handler;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.repository.UserRepo;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepo userRepo;

    public OAuth2LoginSuccessHandler(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");

        User user = userRepo.findByEmail(email);
        if (user == null) {
            user = User.builder()
                    .email(email)
                    .fullName(name)
                    .active(true)
                    .role("CUSTOMER")
                    .build();
            userRepo.saveUserFromGoogle(user);
        }

        if (!user.isActive()) {
            response.sendRedirect("/login?error=inactive");
            return;
        }

        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(user, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authToken);

        request.getSession().setAttribute("user", user);

        switch (user.getRole()) {
            case "ADMIN" -> response.sendRedirect("/admin-dashboard");
            case "MODERATOR" -> response.sendRedirect("/moderator-dashboard");
            case "HOTEL OWNER" -> response.sendRedirect("/host-dashboard");
            default -> response.sendRedirect("/home");
        }

    }
}

