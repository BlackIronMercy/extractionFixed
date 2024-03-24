package com.main.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class JwtRequest implements Serializable {

	private String username;
	private String password;
	private JWTUserDetails userDetails;
	private String csrft;
	private String captchaInput;
	private String encCaptcha;
	private Object captchaImg;
	private String csrf2;
	private String csrf;
	private String loginFlag;
	
}