package com.triquang;

import com.triquang.controller.AuthController;
import com.triquang.model.User;
import com.triquang.request.LoginRequest;
import com.triquang.response.JwtResponse;
import com.triquang.security.WebUserDetails;
import com.triquang.security.jwt.JwtUtils;
import com.triquang.service.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class AuthControllerTest {

    private AuthController authController;
    private IUserService userService;
    private AuthenticationManager authenticationManager;
    private JwtUtils jwtUtils;

    @BeforeEach
    public void setup() {
        userService = mock(IUserService.class);
        authenticationManager = mock(AuthenticationManager.class);
        jwtUtils = mock(JwtUtils.class);
        authController = new AuthController(userService, authenticationManager, jwtUtils);
    }

    @Test
    public void testAuthenticateUser_Success() {
        // Given
        LoginRequest request = new LoginRequest("test@example.com", "password");
        WebUserDetails userDetails = new WebUserDetails(1L, "test@example.com", "password", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        String jwtToken = "test_jwt_token";

        // Mocking
        when(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())))
                .thenReturn(authentication);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn(jwtToken);

        // When
        ResponseEntity<?> responseEntity = authController.authenticateUser(request);

        // Then
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(JwtResponse.class, responseEntity.getBody().getClass());
        JwtResponse jwtResponse = (JwtResponse) responseEntity.getBody();
        assertEquals(userDetails.getUsername(), jwtResponse.getEmail());
        assertEquals(jwtToken, jwtResponse.getToken());
        assertEquals(1L, jwtResponse.getId());
        assertEquals(1, jwtResponse.getRoles().size());
        assertEquals("ROLE_USER", jwtResponse.getRoles().get(0));
    }

    @Test
    public void testAuthenticateUser_InvalidCredentials() {
        // Given
        LoginRequest request = new LoginRequest("test@example.com", "invalid_password");

        // Mocking
        when(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // When
        ResponseEntity<?> responseEntity = authController.authenticateUser(request);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
        assertEquals("Invalid credentials", responseEntity.getBody());
    }

    @Test
    public void testAuthenticateUser_NullRequest() {
        // Given a null login request
        LoginRequest request = null;

        // Mocking authentication manager to throw AuthenticationException
        when(authenticationManager.authenticate(null))
                .thenThrow(AuthenticationException.class);

        // When
        ResponseEntity<?> responseEntity = authController.authenticateUser(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("Invalid login request", responseEntity.getBody());
    }
    @Test
    public void testAuthenticateUser_EmptyEmail() {
        // Given
        LoginRequest request = new LoginRequest("", "password");

        // Mocking authentication manager to return null, indicating authentication failure
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // When
        ResponseEntity<?> responseEntity = authController.authenticateUser(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("Email cannot be empty", responseEntity.getBody());
    }
}
