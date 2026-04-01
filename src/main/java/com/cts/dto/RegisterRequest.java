package com.cts.dto;

import com.cts.entity.Role;
import lombok.Data;

@Data
public class RegisterRequest {
	private String name;
	private String email;
	private String password;
	private String phone;
	private Role role;
}