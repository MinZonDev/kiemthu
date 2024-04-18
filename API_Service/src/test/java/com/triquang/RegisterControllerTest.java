package com.triquang;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.triquang.exception.UserException;
import com.triquang.model.Role;
import com.triquang.model.User;
import com.triquang.repository.RoleRepository;
import com.triquang.repository.UserRepository;
import com.triquang.service.impl.UserServiceImpl;

import java.util.Collections;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class RegisterControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    public void testRegisterUser_Success() {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");

        Role userRole = new Role();
        userRole.setName("ROLE_USER");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        User registeredUser = userService.registerUser(user);

        // Assert
        assertEquals("test@example.com", registeredUser.getEmail());
        assertEquals("encodedPassword", registeredUser.getPassword());
        assertEquals(Collections.singletonList(userRole), registeredUser.getRoles());
    }

    @Test
    public void testRegisterUser_UserAlreadyExists() {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // Act & Assert
        UserException exception = org.junit.jupiter.api.Assertions.assertThrows(UserException.class, () -> {
            userService.registerUser(user);
        });

        assertEquals("test@example.com already exists", exception.getMessage());
    }

    @Test
    public void testRegisterUser_RoleNotFound() {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());

        // Act & Assert
        UserException exception = org.junit.jupiter.api.Assertions.assertThrows(UserException.class, () -> {
            userService.registerUser(user);
        });

        assertEquals("Role not found", exception.getMessage());
    }

    @Test
    public void testRegisterUser_NullUser() {
        // Act & Assert
        IllegalArgumentException exception = org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(null);
        });

        assertEquals("User cannot be null", exception.getMessage());
    }


}

