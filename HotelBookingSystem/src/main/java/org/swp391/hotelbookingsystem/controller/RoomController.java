package org.swp391.hotelbookingsystem.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RoomController {
    @GetMapping("/room-list")
    public String roomList(){
        return "page/roomList";
    }
}
