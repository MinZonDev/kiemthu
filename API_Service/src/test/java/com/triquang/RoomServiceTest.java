package com.triquang;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.rowset.serial.SerialBlob;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;

import com.triquang.exception.InternalServerException;
import com.triquang.exception.ResourceNotFoundException;
import com.triquang.model.Room;
import com.triquang.repository.RoomRepository;
import com.triquang.service.impl.RoomServiceImpl;

@SpringBootTest
public class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private RoomServiceImpl roomService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testUpdateRoom_Success() throws SQLException {
        // Given
        Long roomId = 1L;
        String roomType = "Standard";
        BigDecimal roomPrice = BigDecimal.valueOf(100);
        byte[] photoBytes = {0, 1, 1, 0};

        Room room = new Room(roomId, roomType, roomPrice);
        room.setImage(new SerialBlob(photoBytes));

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        // When
        Room updatedRoom = roomService.updateRoom(roomId, "Deluxe", BigDecimal.valueOf(150), photoBytes);

        // Then
        assertNotNull(updatedRoom);
        assertEquals("Deluxe", updatedRoom.getRoomType());
        assertEquals(BigDecimal.valueOf(150), updatedRoom.getRoomPrice());
    }

    @Test
    public void testUpdateRoom_InvalidRoomId() {
        // Given
        Long roomId = 2L;
        String roomType = "Standard";
        BigDecimal roomPrice = BigDecimal.valueOf(100);
        byte[] photoBytes = {0, 1, 1, 0};

        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        // When, Then
        try {
            roomService.updateRoom(roomId, roomType, roomPrice, photoBytes);
        } catch (ResourceNotFoundException ex) {
            assertEquals("Room not found", ex.getMessage());
        }
    }

    @Test
    public void testUpdateRoom_NullPhotoBytes() throws SQLException {
        // Given
        Long roomId = 1L;
        String roomType = "Standard";
        BigDecimal roomPrice = BigDecimal.valueOf(100);

        Room room = new Room(roomId, roomType, roomPrice);

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        // When
        Room updatedRoom = roomService.updateRoom(roomId, "Deluxe", BigDecimal.valueOf(150), null);

        // Then
        assertNotNull(updatedRoom);
        assertEquals("Deluxe", updatedRoom.getRoomType());
        assertEquals(BigDecimal.valueOf(150), updatedRoom.getRoomPrice());
        assertEquals(null, updatedRoom.getImage());
    }

    @Test
    public void testUpdateRoom_InternalServerError() throws SQLException {
        // Given
        Long roomId = 1L;
        String roomType = "Standard";
        BigDecimal roomPrice = BigDecimal.valueOf(100);
        byte[] photoBytes = {0, 1, 1, 0};

        Room room = new Room(roomId, roomType, roomPrice);

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(roomRepository.save(room)).thenThrow(new InternalServerException("Error updating room"));

        // When, Then
        try {
            roomService.updateRoom(roomId, "Deluxe", BigDecimal.valueOf(150), photoBytes);
        } catch (InternalServerException ex) {
            assertEquals("Error updating room", ex.getMessage());
        }
    }
}
