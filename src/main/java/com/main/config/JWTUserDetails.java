package com.main.config;

import java.util.List;

import lombok.Data;

@Data
public class JWTUserDetails {

	private String role;
	private Long userId;
	private String username;
	private List<Long> designationId;
	private List<Long> departmentId;
	private List<String> projects;
}
