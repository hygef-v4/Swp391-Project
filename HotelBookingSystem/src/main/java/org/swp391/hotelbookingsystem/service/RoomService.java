package org.swp391.hotelbookingsystem.service;

import java.sql.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.swp391.hotelbookingsystem.model.Room;
import org.swp391.hotelbookingsystem.repository.RoomRepository;

@Service
public class RoomService {
    private final RoomRepository roomRepository;

    @Autowired
    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public List<Room> getRoomsByIdAndDateRange(int id, Date checkin, Date checkout){
        List<Room> rooms = roomRepository.getRoomsByIdAndDateRange(id, checkin, checkout);

        for (Room room : rooms) {
            room.setImages(roomRepository.getRoomImages(room.getRoomId()));
        }

        return rooms;
    }

    public List<Room> getRoomByHotelId(int id) {
        List<Room> rooms = roomRepository.getRoomsByHotelId(id);

        for (Room room : rooms) {
            room.setImages(roomRepository.getRoomImages(room.getRoomId()));
        }

        return rooms;
    }

    public void saveRoom(Room room, List<Integer> amenityIds, List<String> imageUrls) {
        // Step 1: Save the room and get its generated room_id
        int roomId = roomRepository.insertRoom(room);

        // Step 2: Save room images
        if (imageUrls != null) {
            for (String url : imageUrls) {
                roomRepository.insertRoomImage(roomId, url);
            }
        }

        //  Link room to amenities
        if (amenityIds != null) {
            for (Integer amenityId : amenityIds) {
                roomRepository.linkRoomAmenity(roomId, amenityId);
            }
        }
    }

    public int countRooms() {
        return roomRepository.countRooms();
    }

    public List<Room> getRoomsByHotelId(int hotelId) {
        return roomRepository.getRoomsByHotelId(hotelId);
    }

    public int getTotalRoomsByHostId(int hostId) {
        return roomRepository.getTotalRoomsByHostId(hostId);
    }

    public void updateRoom(Room room, List<Integer> amenityIds, List<String> imageUrls) {
        roomRepository.updateRoom(room);
        
        // Update amenities if provided
        if (amenityIds != null) {
            // Clear existing amenities first
            roomRepository.clearRoomAmenities(room.getRoomId());
            // Add new amenities
            for (Integer amenityId : amenityIds) {
                roomRepository.linkRoomAmenity(room.getRoomId(), amenityId);
            }
        }
        
        // Update images if provided
        if (imageUrls != null && !imageUrls.isEmpty()) {
            // Clear existing images first
            roomRepository.clearRoomImages(room.getRoomId());
            // Add new images
            for (String url : imageUrls) {
                roomRepository.insertRoomImage(room.getRoomId(), url);
            }
        }
    }

    public void deleteRoom(int roomId) {
        roomRepository.deleteRoom(roomId);
    }

    public int countBookedRoomsByHostId(int hostId) {
        return roomRepository.countBookedRoomsByHostId(hostId);
    }

    public int countAvailableRoomsByHotelId(int hotelId) {
        return roomRepository.countAvailableRoomsByHotelId(hotelId);
    }


    public Room getRoomById(int roomId) {
        return roomRepository.getRoomById(roomId);
    }

    public boolean hasActiveBookingUnits(int roomId) {
        return roomRepository.hasActiveBookingUnits(roomId);
    }
}
