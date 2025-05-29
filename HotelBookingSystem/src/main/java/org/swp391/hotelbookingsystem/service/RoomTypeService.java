package org.swp391.hotelbookingsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.swp391.hotelbookingsystem.model.RoomTypes;
import org.swp391.hotelbookingsystem.repository.RoomTypeRepository;

import java.util.List;

@Service
public class RoomTypeService {

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    public List<RoomTypes> getAllRoomTypes() {
        return roomTypeRepository.getAllRoomTypes();
    }

    public RoomTypes getRoomTypeById(int roomTypeId) {
        return roomTypeRepository.getRoomTypeById(roomTypeId);
    }
}