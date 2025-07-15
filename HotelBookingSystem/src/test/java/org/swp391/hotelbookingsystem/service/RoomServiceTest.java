package org.swp391.hotelbookingsystem.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.swp391.hotelbookingsystem.model.Room;
import org.swp391.hotelbookingsystem.repository.RoomRepository;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private RoomService roomService;

    private Room testRoom;

    @BeforeEach
    void setUp() {
        testRoom = new Room();
        testRoom.setRoomId(1);
        testRoom.setHotelId(100);
        testRoom.setTitle("Deluxe Room");
    }

    @Test
    void testGetRoomByHotelId_Success() {
        List<Room> rooms = Collections.singletonList(testRoom);
        List<String> images = Arrays.asList("image1.jpg", "image2.jpg");

        when(roomRepository.getRoomsByHotelId(100)).thenReturn(rooms);
        when(roomRepository.getRoomImages(1)).thenReturn(images);

        List<Room> result = roomService.getRoomByHotelId(100);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getImages().size());
        verify(roomRepository).getRoomsByHotelId(100);
        verify(roomRepository).getRoomImages(1);
    }



    @Test
    void testGetAvailableRoomsByHotelId_Success() {
        List<Room> rooms = Collections.singletonList(testRoom);
        List<String> images = Arrays.asList("image1.jpg");

        when(roomRepository.getAvailableRoomsByHotelId(100)).thenReturn(rooms);
        when(roomRepository.getRoomImages(1)).thenReturn(images);

        List<Room> result = roomService.getAvailableRoomsByHotelId(100);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getImages().size());
        verify(roomRepository).getAvailableRoomsByHotelId(100);
        verify(roomRepository).getRoomImages(1);
    }



    @Test
    void testSaveRoom_WithAmenitiesAndImages() {
        List<Integer> amenityIds = Arrays.asList(1, 2);
        List<String> imageUrls = Arrays.asList("url1", "url2");

        when(roomRepository.insertRoom(any(Room.class))).thenReturn(1);
        doNothing().when(roomRepository).insertRoomImage(anyInt(), anyString());
        doNothing().when(roomRepository).linkRoomAmenity(anyInt(), anyInt());

        roomService.saveRoom(testRoom, amenityIds, imageUrls);

        verify(roomRepository).insertRoom(testRoom);
        verify(roomRepository, times(2)).insertRoomImage(eq(1), anyString());
        verify(roomRepository, times(2)).linkRoomAmenity(eq(1), anyInt());
    }

    @Test
    void testSaveRoom_WithoutAmenitiesAndImages() {
        when(roomRepository.insertRoom(any(Room.class))).thenReturn(1);

        roomService.saveRoom(testRoom, null, null);

        verify(roomRepository).insertRoom(testRoom);
        verify(roomRepository, never()).insertRoomImage(anyInt(), anyString());
        verify(roomRepository, never()).linkRoomAmenity(anyInt(), anyInt());
    }



    @Test
    void testCountRooms() {
        when(roomRepository.countRooms()).thenReturn(10);
        int count = roomService.countRooms();
        assertEquals(10, count);
        verify(roomRepository).countRooms();
    }

    @Test
    void testGetRoomsByHotelId() {
        List<Room> rooms = Collections.singletonList(testRoom);
        when(roomRepository.getRoomsByHotelId(100)).thenReturn(rooms);
        List<Room> result = roomService.getRoomsByHotelId(100);
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(roomRepository).getRoomsByHotelId(100);
    }

    @Test
    void testGetTotalRoomsByHostId() {
        when(roomRepository.getTotalRoomsByHostId(1)).thenReturn(5);
        int count = roomService.getTotalRoomsByHostId(1);
        assertEquals(5, count);
        verify(roomRepository).getTotalRoomsByHostId(1);
    }

    @Test
    void testUpdateRoom_WithAmenitiesAndImages() {
        List<Integer> amenityIds = Arrays.asList(3, 4);
        List<String> imageUrls = Arrays.asList("new_url1", "new_url2");

        doNothing().when(roomRepository).updateRoom(any(Room.class));
        doNothing().when(roomRepository).clearRoomAmenities(anyInt());
        doNothing().when(roomRepository).linkRoomAmenity(anyInt(), anyInt());
        doNothing().when(roomRepository).clearRoomImages(anyInt());
        doNothing().when(roomRepository).insertRoomImage(anyInt(), anyString());

        roomService.updateRoom(testRoom, amenityIds, imageUrls);

        verify(roomRepository).updateRoom(testRoom);
        verify(roomRepository).clearRoomAmenities(1);
        verify(roomRepository, times(2)).linkRoomAmenity(eq(1), anyInt());
        verify(roomRepository).clearRoomImages(1);
        verify(roomRepository, times(2)).insertRoomImage(eq(1), anyString());
    }

    @Test
    void testUpdateRoom_WithoutAmenitiesAndImages() {
        doNothing().when(roomRepository).updateRoom(any(Room.class));

        roomService.updateRoom(testRoom, null, null);

        verify(roomRepository).updateRoom(testRoom);
        verify(roomRepository, never()).clearRoomAmenities(anyInt());
        verify(roomRepository, never()).linkRoomAmenity(anyInt(), anyInt());
        verify(roomRepository, never()).clearRoomImages(anyInt());
        verify(roomRepository, never()).insertRoomImage(anyInt(), anyString());
    }
    
    @Test
    void testUpdateRoom_WithEmptyImageUrls() {
        doNothing().when(roomRepository).updateRoom(any(Room.class));

        roomService.updateRoom(testRoom, new ArrayList<>(), new ArrayList<>());

        verify(roomRepository).updateRoom(testRoom);
        verify(roomRepository, never()).clearRoomImages(anyInt());
        verify(roomRepository, never()).insertRoomImage(anyInt(), anyString());
    }


    @Test
    void testDeleteRoom() {
        doNothing().when(roomRepository).deleteRoom(1);
        roomService.deleteRoom(1);
        verify(roomRepository).deleteRoom(1);
    }

    @Test
    void testCountBookedRoomsByHostId() {
        when(roomRepository.countBookedRoomsByHostId(1)).thenReturn(2);
        int count = roomService.countBookedRoomsByHostId(1);
        assertEquals(2, count);
        verify(roomRepository).countBookedRoomsByHostId(1);
    }

    @Test
    void testCountAvailableRoomsByHotelId() {
        when(roomRepository.countAvailableRoomsByHotelId(100)).thenReturn(8);
        int count = roomService.countAvailableRoomsByHotelId(100);
        assertEquals(8, count);
        verify(roomRepository).countAvailableRoomsByHotelId(100);
    }


    @Test
    void testGetRoomById_NotFound() {
        when(roomRepository.getRoomById(999)).thenReturn(null);
        Room result = roomService.getRoomById(999);
        assertNull(result);
        verify(roomRepository).getRoomById(999);
    }



    @Test
    void testHasActiveBookingUnits_False() {
        when(roomRepository.hasActiveBookingUnits(2)).thenReturn(false);
        boolean result = roomService.hasActiveBookingUnits(2);
        assertFalse(result);
        verify(roomRepository).hasActiveBookingUnits(2);
    }
} 