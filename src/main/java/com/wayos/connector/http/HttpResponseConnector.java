package com.wayos.connector.http;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.wayos.connector.ResponseConnector;
import com.wayos.connector.ResponseObject;

public class HttpResponseConnector extends ResponseConnector {

	public HttpResponseConnector(HttpRequestObject requestObject) {
		super(requestObject);
	}
	
	private HttpRequestObject httpRequestObject() {
		return (HttpRequestObject) super.requestObject;
	}	

	@Override
	public String execute(ResponseObject responseObject) {
		
		HttpServletRequest request = httpRequestObject().httpServletRequest();
		
		HttpServletResponse response = httpRequestObject().httpServletResponse();
		
    	String contentType = request.getHeader("Content-Type");
    	
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setCharacterEncoding("UTF-8");

        String result;
        
        /**
         * First try with json
         */
        try {
        	
            if (contentType!=null && contentType.equals("application/json")) {
            	
                response.setContentType("application/json");
                
            	result = responseObject.toJSONString();
            	
                response.getWriter().print(result);
            	
            	return result;
            } 
        	
            response.setContentType("text/plain");
            
            result = responseObject.toString();	
            
            response.getWriter().print(result);
            
            return result;
            
        } catch (Exception e) {
        	
        	throw new RuntimeException(e);
        	
        }

	}

}
