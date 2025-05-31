package org.swp391.hotelbookingsystem.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.swp391.hotelbookingsystem.model.Room;
import org.swp391.hotelbookingsystem.repository.RoomRepository;

@Service
public class RoomService {
    @Autowired
    RoomRepository roomRepository;

    public List<Room> getRoomByHotelId(int id){
        List<Room> rooms = roomRepository.getRoomByHotelId(id);

        for(Room room : rooms){
            room.setImages(roomRepository.getRoomImages(room.getRoomId()));
        }

        return rooms;
    }
}
