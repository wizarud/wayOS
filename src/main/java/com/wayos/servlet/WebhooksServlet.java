package com.wayos.servlet;

import javax.servlet.annotation.WebServlet;

import com.wayos.Application;
import com.wayos.Context;
import com.wayos.Session;
import com.wayos.connector.ResponseConnector;
import com.wayos.connector.ResponseObject;
import com.wayos.connector.SessionPool;
import com.wayos.connector.http.HttpRequestObject;

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
        
		/**
		 * TODO: Do we need to protect webhook API with public property? 
		 * in case of limit access for author only
		 * PlayServlet > WebHookServlet (Key Header chk)
		 * 
		 * TODO: use secret from SECRET configuration in API page!
		 * SignatureValidator signatureValidator = new SignatureValidator(Configuration.privatePath / secret in publish);
		 * 
		 * show responseText as "You dont have authorize to access this content"
		 */
		Context context = sessionPool.getContext(requestObject.contextName());
		
		String isPublishString = context.prop().get("publish");
				
		boolean isPublish = isPublishString != null && isPublishString.equalsIgnoreCase("true");
		
		//System.out.println("isPublishString: " + isPublishString);
		//System.out.println(requestObject.contextName() + ": Publish (" + isPublish + ")");
		
		String responseText;
		
		if (isPublish || requestObject.messageObject().attr("selfSign") != null) {
			
			responseText = session.parse(requestObject.messageObject());
			
		} else {
			
			/**
			 * TODO: Use secret from privates/<accountId>/<botId>.secret.json
			 * and SignatureValidator to support API Calling
			 */
			
			responseText = "This content is private!";
			
		}             
				
		//For logging message
		String log = responseConnector.execute(new ResponseObject(responseText));
						
	}

}
