package com.main.common;

import lombok.Data;

@Data
public class DataContainer {

	private Object data;
	private String status;
	private Object errorObject;
	private String msg;
	private String captcha;
	
}
