package com.wayos.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.wayos.Application;
import com.wayos.Context;
import com.wayos.connector.ResponseConnector;
import com.wayos.connector.SessionPool;
import com.wayos.connector.facebook.FacebookHttpRequestObject;
import com.wayos.connector.facebook.FacebookResponseConnector;
import com.wayos.connector.http.HttpRequestObject;
import com.wayos.connector.http.HttpResponseConnector;
import com.wayos.connector.line.LINEHttpRequestObject;
import com.wayos.connector.line.LINEResponseConnector;
import com.wayos.connector.rest.RestHttpRequestObject;
import com.wayos.connector.rest.RestResponseConnector;
import com.wayos.connector.web.WebHttpRequestObject;
import com.wayos.connector.web.WebResponseConnector;
import com.wayos.util.ConsoleUtil;
import com.wayos.util.URItoContextResolver;

@SuppressWarnings("serial")
public abstract class WAYOSServlet extends HttpServlet {
	
	private final HttpRequestObject createRequestObject(HttpServletRequest request, HttpServletResponse response) {
		
		/**
		 * LINE
		 */
		if (request.getHeader("X-Line-Signature") != null) {
			
			return new LINEHttpRequestObject(request);
			
		}
		
		/**
		 * Facebook
		 */
		if (request.getHeader("X-Hub-Signature") != null) {
			
			return new FacebookHttpRequestObject(request);
			
		}
		
		/**
		 * Web (Homepage)
		 */
		if (request.getHeader("User-Agent") != null && 
				request.getHeader("User-Agent").startsWith("Mozilla")) {
			
			//return new WebHttpRequestObject(request, response); //TODO: Uncomment for use old version
			
			return new RestHttpRequestObject(request, response);
		}
		
		/**
		 * Default Http Request (REST API)
		 */
		return new HttpRequestObject(request, response);
	}
	
	private final ResponseConnector createResponseConnector(HttpRequestObject requestObject) {
		
		/**
		 * LINE
		 */
		if (requestObject instanceof LINEHttpRequestObject) {
			
			return new LINEResponseConnector((LINEHttpRequestObject) requestObject);
		}
		
		/**
		 * Facebook
		 */
		if (requestObject instanceof FacebookHttpRequestObject) {
			
			return new FacebookResponseConnector((FacebookHttpRequestObject) requestObject);
		}
		
		/**
		 * Web (Homepage)
		 */
		if (requestObject instanceof WebHttpRequestObject) {
			
			return new WebResponseConnector((WebHttpRequestObject) requestObject);
		}
		
		/**
		 * Web (Homepage)
		 */
		if (requestObject instanceof RestHttpRequestObject) {
			
			return new RestResponseConnector((RestHttpRequestObject) requestObject);
		}
		
		/**
		 * Default Http Response (REST API)
		 */
		return new HttpResponseConnector(requestObject);
	}
	
	@Override
	protected final void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		/**
		 * For facebook verification
		 * EAAGyi7keSpcBAJ4CNALXPqbcN2HpraCOGOzumLYnKcKpdl3oTGE2o1IZCgqKAZALpUPA3cdlhnUMdcRfHVt3SZBgD6breCe6yE5B1A81UuPl7inuyZBVRj80jlC3PsXMrFRhvuG1ZCTaPKJivXizFPr6VljErS2bEL1ksQr6fUgZDZD
		 */
		final String VERIFY_TOKEN = "alpaca";
		String mode = req.getParameter("hub.mode");
		String token = req.getParameter("hub.verify_token");
		String challenge = req.getParameter("hub.challenge");
		
		if (token!=null) {	
			if (token.equals(VERIFY_TOKEN)) {
				resp.getWriter().print(challenge);
				return;				
			}
			resp.sendError(403);
			return;
		}
		
	}
       
	@Override
	protected final void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		request.setCharacterEncoding("UTF-8");
		
		HttpRequestObject requestObject = createRequestObject(request, response);
		
		ResponseConnector responseConnector = createResponseConnector(requestObject);
		
		doAction(requestObject, responseConnector);		
	}
		
	protected final void appendLogs(Long userTimestamp, String accountId, String botId, String channel, String sessionId, String inputText, String responseText) {
		
		/**
		 * Remove @Id for original Message in case of from choice
		 */
		String [] msgs = inputText.split(" ");
		String message = "";
		for (String msg:msgs) {
			if (!msg.startsWith("@")) {
				message += msg + " ";
			}
		}
		message = message.trim();
				
		/**
		 * Record Chatlog
		 */
		
		ConsoleUtil consoleUtil = Application.instance().get(ConsoleUtil.class);
				
		consoleUtil.appendMessage(userTimestamp, accountId, botId, channel, sessionId, "User>> " + message);
		
		consoleUtil.appendMessage(null, accountId, botId, channel, sessionId, "Bot>> " + responseText);

	}
	
	/**
	 * Override your service here!
	 * Default use sessionPool to create ResponseObject
	 * @param requestObject
	 * @param responseObject
	 */
	protected abstract void doAction(HttpRequestObject requestObject, ResponseConnector responseConnector);

}
