package org.swp391.hotelbookingsystem.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    // This method handles the root URL ("/") and returns the name of the view to be rendered.
    // The view name is "index", which typically corresponds to an HTML file in a templates directory.
    @GetMapping("/")
    public String home() {
        return "page/homepage";
    }
}
