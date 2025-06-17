package org.swp391.hotelbookingsystem.service;

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

    public List<Room> getRoomByHotelId(int id) {
        List<Room> rooms = roomRepository.getRoomsByHotelId(id);

        for (Room room : rooms) {
            room.setImages(roomRepository.getRoomImages(room.getRoomId()));
        }

        return rooms;
    }

    public List<Room> getAvailableRoomsByHotelId(int hotelId) {
        List<Room> rooms = roomRepository.getAvailableRoomsByHotelId(hotelId);

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

    /**
     * Returns all rooms associated with a given hotel.
     */
    public List<Room> getRoomsByHotelId(int hotelId) {
        return roomRepository.getRoomsByHotelId(hotelId);
    }

    public int getTotalRoomsByHostId(int hostId) {
        return roomRepository.getTotalRoomsByHostId(hostId);
    }
}
