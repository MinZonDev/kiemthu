package com.triquang.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.triquang.exception.UserException;
import com.triquang.model.User;
import com.triquang.request.LoginRequest;
import com.triquang.response.JwtResponse;
import com.triquang.security.WebUserDetails;
import com.triquang.security.jwt.JwtEntryPoint;
import com.triquang.security.jwt.JwtUtils;
import com.triquang.service.IUserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final IUserService userService;
	private final AuthenticationManager authenticationManager;
	private final JwtUtils jwtUtils;

	@Autowired
	public AuthController(IUserService userService, AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
		this.userService = userService;
		this.authenticationManager = authenticationManager;
		this.jwtUtils = jwtUtils;
	}

	@PostMapping("/register")
	public ResponseEntity<?> registerUser(@RequestBody User user) {
		if (user == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User cannot be null");
		}
		if (!isValidEmail(user.getEmail())) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid email format");
		}
		try {
			userService.registerUser(user);
			return ResponseEntity.ok("Registration successful!");
		} catch (UserException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		}
	}

	private boolean isValidEmail(String email) {
		// You can implement your own email validation logic here
		// This is just a simple example
		return email != null && email.matches("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$");
	}

	@PostMapping("/login")
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest request) {
		try {
			Authentication authentication = authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

			SecurityContextHolder.getContext().setAuthentication(authentication);
			String jwt = jwtUtils.generateJwtToken(authentication);
			WebUserDetails userDetails = (WebUserDetails) authentication.getPrincipal();
			List<String> roles = userDetails.getAuthorities()
					.stream()
					.map(GrantedAuthority::getAuthority)
					.collect(Collectors.toList());
			return ResponseEntity.ok(new JwtResponse(userDetails.getId(), userDetails.getEmail(), jwt, roles));
		} catch (BadCredentialsException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
		}
	}
}