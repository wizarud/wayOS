package com.wayos.servlet.example;

import javax.servlet.annotation.WebServlet;

import com.wayos.connector.ResponseConnector;
import com.wayos.connector.ResponseObject;
import com.wayos.connector.http.HttpRequestObject;
import com.wayos.servlet.WAYOSServlet;

/**
 * Simple echo message
 * You can get the parameters from endpoints such as
 * contextName - action mapping URI
 * sessionId - roomId/userId for LINE, pageId for Facebook Page, localSessionId for Web
 * messageObject - message with embeded parameters such as wordCount
 */
@WebServlet("/echo/*")
public class EchoServlet extends WAYOSServlet {

	@Override
	protected void doAction(HttpRequestObject requestObject, ResponseConnector responseConnector) {
			
		/*
		requestObject.contextName();
		requestObject.sessionId();
		*/
		
		String responseText = requestObject.messageObject().toString();

		responseConnector.execute(new ResponseObject(responseText));
		
	}

}
