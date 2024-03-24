package com.main.model;

import java.io.Serializable;

public class JwtResponse implements Serializable {

	private static final long serialVersionUID = -8091879091924046844L;
	private final String token;
	private final String responseStatus;
	private final String loginFlag;

	public JwtResponse(String token, String jwtresponse, String loginFlag) {
		this.token = token;
		this.responseStatus = jwtresponse;
		this.loginFlag = loginFlag;
	}

	public String getToken() {
		return token;
	}

	public String getResponseStatus() {
		return responseStatus;
	}

	public String getLoginFlag() {
		return loginFlag;
	}

	
	
}