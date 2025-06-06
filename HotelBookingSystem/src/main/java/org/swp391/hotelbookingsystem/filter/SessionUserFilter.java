package org.swp391.hotelbookingsystem.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.repository.UserRepo;

import java.io.IOException;

@Component
public class SessionUserFilter implements Filter {

    private final UserRepo userRepo;

    public SessionUserFilter(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpSession session = httpReq.getSession(false);

        if (session != null && session.getAttribute("user") == null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                String email = auth.getName();
                User user = userRepo.findByEmail(email);
                if (user != null) {
                    session.setAttribute("user", user);
                }
            }
        }

        chain.doFilter(request, response);
    }
}
