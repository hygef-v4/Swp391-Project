package org.swp391.hotelbookingsystem.controller.moderator;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ModeratorUserListController {

    @GetMapping("/moderator-user-list")
    public String getUserList() {
        return "moderator/moderatorUserList";
    }
}

