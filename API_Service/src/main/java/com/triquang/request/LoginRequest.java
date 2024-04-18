package com.triquang.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor // Thêm constructor không tham số
public class LoginRequest {

	@NotBlank
	private String email;

	@NotBlank
	private String password;

	// Constructor có tham số
	public LoginRequest(String email, String password) {
		this.email = email;
		this.password = password;
	}
}
