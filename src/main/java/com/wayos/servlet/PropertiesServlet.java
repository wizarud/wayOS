package com.wayos.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.wayos.Context;
import com.wayos.servlet.console.ConsoleServlet;

import x.org.json.JSONObject;

@SuppressWarnings("serial")
@WebServlet("/props/*")
public class PropertiesServlet extends ConsoleServlet {
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		String contextName = contextName(req.getRequestURI(), true);
		
		String sessionId = req.getParameter("sessionId");

		String [] tokens = contextName.split("/");
		String accountId = tokens[0];
		String botId = tokens[1];
		
		try {
			
			Context context = sessionPool().getContext(contextName);
			
			context.load();//Reload for update viewCount

			JSONObject properties = new JSONObject(context.prop());
				
			/**
			 * New Session
			 */
			if (sessionId==null) {
				
				/**
				 * To protect duplicate session id for multiapp in same web browser
				 * I use contextName prefix
				 * <contextName>-<currentTimeMS>
				 */
				sessionId = sessionPool().generateSessionId();
				
			}
			
			properties.put("sessionId", sessionId);
			properties.put("viewCount", "" + consoleUtil().sessionCount(accountId, botId, "web"));
			
			properties.remove("greeting");
			properties.remove("silent");
			properties.remove("unknown");
			
			
			resp.setHeader("Access-Control-Allow-Origin", "*");
			resp.setContentType("application/json");
			resp.setCharacterEncoding("UTF-8");
			
			resp.getWriter().print(properties.toString());
			
		} catch (Exception e) {
			
			throw new RuntimeException(e);
			
		}
	}

}
