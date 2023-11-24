package com.wayos.connector.http;

import javax.servlet.http.HttpServletResponse;

import com.wayos.connector.ResponseObject;

public class HttpResponseObject extends ResponseObject {
	
	private final HttpServletResponse response;

	public HttpResponseObject(String responseText, HttpServletResponse toResponse) {
		super(responseText);
		this.response = toResponse;
	}
	
	public HttpServletResponse httpServletResponse() {
		return response;
	}

}
