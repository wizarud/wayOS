package com.wayos.servlet;

import javax.servlet.annotation.WebServlet;

import com.wayos.Session;
import com.wayos.connector.ResponseConnector;
import com.wayos.connector.ResponseObject;
import com.wayos.connector.SessionPool;
import com.wayos.connector.http.HttpRequestObject;
import com.wayos.util.Application;

@SuppressWarnings("serial")
@WebServlet("/webhooks/*")
public class WebhooksServlet extends WAYOSServlet {
	
	protected void doAction(HttpRequestObject requestObject, ResponseConnector responseConnector) {
		
		/**
		 * TODO: Default parsing be use brainy context system
		 */
		SessionPool sessionPool = Application.instance().get(SessionPool.class);
				
        Session session = sessionPool.get(requestObject);
        
        requestObject.prepare(session);
        
		String responseText = session.parse(requestObject.messageObject());

		responseConnector.execute(new ResponseObject(responseText));
	}	

}
