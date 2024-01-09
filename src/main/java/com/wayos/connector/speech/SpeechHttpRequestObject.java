package com.wayos.connector.speech;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.wayos.Session;
import com.wayos.connector.http.HttpRequestObject;

public class SpeechHttpRequestObject extends HttpRequestObject {
	
	String language;
	
	public SpeechHttpRequestObject(HttpServletRequest request, HttpServletResponse response) {
		
		super(request, response);
				
        messageObject.attr("channel", "speech");
    }
		
	@Override
	public void prepare(Session session) {
		
		/**
		 * Call super if this sessionId is registered admin
		 */
		super.prepare(session);
		
        session.vars("#s_fullName",  "Guest");
        
		language = session.context().prop("language");
		
		if (language==null) {
			language = "en";
		}
		
	}	
	
}
