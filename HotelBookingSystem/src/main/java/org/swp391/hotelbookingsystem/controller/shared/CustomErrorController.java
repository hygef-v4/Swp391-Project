package org.swp391.hotelbookingsystem.controller.shared;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);

        model.addAttribute("status", status);
        model.addAttribute("message", message != null ? message : "Trang bạn yêu cầu không tồn tại hoặc đã bị lỗi.");
        return "page/error"; // Thymeleaf template tại templates/page/error.html
    }

    public String getErrorPath() {
        return "/error";
    }
}
