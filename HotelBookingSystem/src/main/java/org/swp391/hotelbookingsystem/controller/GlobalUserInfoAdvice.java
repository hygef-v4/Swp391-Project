package org.swp391.hotelbookingsystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;
import org.swp391.hotelbookingsystem.model.User;
import org.swp391.hotelbookingsystem.repository.UserRepo;

@ControllerAdvice
public class GlobalUserInfoAdvice {

    @Autowired
    private UserRepo repo;

    @ModelAttribute
    public void addUserInfoToModel(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();

            if (principal instanceof UserDetails user) {
                User u = repo.findByEmail(user.getUsername());
                model.addAttribute("fullName", u.getFullname());
                model.addAttribute("email", u.getEmail());
            }

            if (principal instanceof OAuth2User oauth2User) {
                model.addAttribute("fullName", oauth2User.getAttribute("name"));
                model.addAttribute("email", oauth2User.getAttribute("email"));
            }
        }
    }
}
