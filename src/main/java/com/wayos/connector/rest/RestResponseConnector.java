package com.wayos.connector.rest;

import javax.servlet.http.HttpServletResponse;

import com.wayos.connector.ResponseConnector;
import com.wayos.connector.ResponseObject;

public class RestResponseConnector extends ResponseConnector {

	public RestResponseConnector(RestHttpRequestObject requestObject) {
		super(requestObject);
	}
	
	private RestHttpRequestObject restHttpRequestObject() {
		return (RestHttpRequestObject) super.requestObject;
	}	

	@Override
	public String execute(ResponseObject responseObject) {
		
		/*
		System.out.println("*****************");
		System.out.println(responseObject);
		System.out.println("*****************");
		*/
		
		HttpServletResponse response = restHttpRequestObject().httpServletResponse();
		
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Methods", "POST, GET");
		response.addHeader("Access-Control-Max-Age", "60");
		
     	response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		try {
		
			response.getWriter().print(responseObject.toJSONString());
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
		
		return responseObject.toString();

	}

}
