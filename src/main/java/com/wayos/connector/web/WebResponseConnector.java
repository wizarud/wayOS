package com.wayos.connector.web;

import javax.servlet.http.HttpServletResponse;

import com.wayos.connector.ResponseConnector;
import com.wayos.connector.ResponseObject;

public class WebResponseConnector extends ResponseConnector {

	public WebResponseConnector(WebHttpRequestObject requestObject) {
		super(requestObject);
	}
	
	private WebHttpRequestObject webHttpRequestObject() {
		return (WebHttpRequestObject) super.requestObject;
	}	

	@Override
	public String execute(ResponseObject responseObject) {
		
		HttpServletResponse response = webHttpRequestObject().httpServletResponse();
		
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");
		
		StringBuilder logCollector = new StringBuilder();
		
		String responseMessages;
		try {
		
			responseMessages = WebAPI.instance().createMessages(responseObject);
			logCollector.append(responseMessages);
			
			response.getWriter().print(responseMessages);
			
		} catch (Exception e) {
			logCollector.append(e.getMessage());
		}
		
		return logCollector.toString();


	}

}
