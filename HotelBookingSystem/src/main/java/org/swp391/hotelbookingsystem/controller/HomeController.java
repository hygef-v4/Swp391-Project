package org.swp391.hotelbookingsystem.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.swp391.hotelbookingsystem.constant.ConstantVariables;

@Controller
public class HomeController {
<<<<<<< Updated upstream
    // This method handles the root URL ("/") and returns the name of the view to be rendered.
    // The view name is "index", which typically corresponds to an HTML file in a templates directory.
    @GetMapping("/")
    public String home() {
=======
    @GetMapping({"/", "/home"})

    public String home(Model model) {
        model.addAttribute(ConstantVariables.PAGE_TITLE, "Hamora | Book Comfortable Rooms in Vietnam");
>>>>>>> Stashed changes
        return "page/homepage";
    }
}
