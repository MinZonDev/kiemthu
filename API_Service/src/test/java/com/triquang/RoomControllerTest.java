package com.triquang;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;

import com.triquang.service.impl.RoomServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import com.triquang.exception.InternalServerException;
import com.triquang.model.Room;
import com.triquang.repository.RoomRepository;

@ExtendWith(MockitoExtension.class)
class RoomControllerTest {

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private RoomServiceImpl roomService;

    @Test
    void testAddNewRoom_Success() throws IOException, SQLException {
        // Arrange
        byte[] imageBytes = { 0x01, 0x02, 0x03 }; // Example image bytes
        MultipartFile image = mock(MultipartFile.class);
        when(image.getBytes()).thenReturn(imageBytes);

        String roomType = "Single";
        BigDecimal roomPrice = BigDecimal.valueOf(100);

        Room savedRoom = new Room();
        savedRoom.setId(1L);
        savedRoom.setRoomType(roomType);
        savedRoom.setRoomPrice(roomPrice);

        when(roomRepository.save(any(Room.class))).thenReturn(savedRoom);

        // Act
        Room newRoom = roomService.addNewRoom(image, roomType, roomPrice);

        // Assert
        assertNotNull(newRoom);
        assertEquals(savedRoom.getId(), newRoom.getId());
        assertEquals(roomType, newRoom.getRoomType());
        assertEquals(roomPrice, newRoom.getRoomPrice());
    }

    @Test
    void testAddNewRoom_EmptyImage() throws IOException, SQLException {
        // Arrange
        MultipartFile image = mock(MultipartFile.class);
        when(image.isEmpty()).thenReturn(true);

        String roomType = "Single";
        BigDecimal roomPrice = BigDecimal.valueOf(100);

        // Act and Assert
        assertThrows(IOException.class, () -> roomService.addNewRoom(image, roomType, roomPrice));
    }

    @Test
    void testAddNewRoom_Exception() throws IOException, SQLException {
        // Arrange
        MultipartFile image = mock(MultipartFile.class);
        when(image.getBytes()).thenThrow(IOException.class);

        String roomType = "Single";
        BigDecimal roomPrice = BigDecimal.valueOf(100);

        // Act and Assert
        assertThrows(InternalServerException.class, () -> roomService.addNewRoom(image, roomType, roomPrice));
    }
    @Test
    void testAddNewRoom_MissingRoomTypeAndPrice() throws IOException, SQLException {
        // Arrange
        byte[] imageBytes = { 0x01, 0x02, 0x03 }; // Example image bytes
        MultipartFile image = mock(MultipartFile.class);
        when(image.getBytes()).thenReturn(imageBytes);

        String roomType = null;
        BigDecimal roomPrice = null;

        // Act and Assert
        assertThrows(IllegalArgumentException.class, () -> roomService.addNewRoom(image, roomType, roomPrice));
    }

}

