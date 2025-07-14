package org.swp391.hotelbookingsystem.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.swp391.hotelbookingsystem.model.User;

import jakarta.servlet.http.HttpSession;

@Controller
public class TestController {

    @GetMapping("/test-notifications")
    public String testNotifications(HttpSession session, Model model) {
        // Debug session attributes
        System.out.println("🔍 Session ID: " + session.getId());
        System.out.println("🔍 Session attributes:");
        session.getAttributeNames().asIterator().forEachRemaining(name -> {
            System.out.println("  - " + name + " = " + session.getAttribute(name));
        });
        
        User user = (User) session.getAttribute("user");
        System.out.println("🧪 Test page requested. User: " + (user != null ? user.getId() + " (" + user.getFullName() + ")" : "null"));
        
        if (user != null) {
            model.addAttribute("userId", user.getId());
            model.addAttribute("userName", user.getFullName());
            System.out.println("✅ Added user to model: " + user.getId());
        } else {
            System.out.println("❌ No user found in session");
            model.addAttribute("userId", null);
            model.addAttribute("userName", "Not logged in");
        }
        
        return "test-notifications";
    }
} 