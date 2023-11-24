package com.wayos.servlet.console;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@WebServlet("/console/push/*")
public class PushServlet extends ConsoleServlet {
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String message = req.getParameter("message");
		String keyword = req.getParameter("keyword");
		
		String contextName = contextName(req.getRequestURI(), false);
		
		String [] tokens = contextName.split("/");
		String accountId = tokens[0];
		String botId = tokens[1];
						
		String target = req.getParameter("target");
		
		//Broadcast Message <Call from dashboard>
		if (target==null || target.trim().isEmpty() || target.trim().equals("All")) {
			/**
			 * Parse keyword or just push message
			 */
			if (!keyword.isEmpty()) //Parse keywords
				resp.getWriter().print(pusherUtil().parse(accountId, botId, keyword + " " + message).size());
			else //Or just push message
				resp.getWriter().print(pusherUtil().push(accountId, botId, message).size());
			return;
			
		}
		
		tokens = target.split("/");
		String channel = tokens[0];
		String sessionId = tokens[1];
						
		/**
		 * Parse keyword or just push message
		 */
		if (!keyword.isEmpty()) //Parse keywords
			pusherUtil().parse(accountId, botId, channel, sessionId, keyword + " " + message);
		else //Or just push message
			pusherUtil().push(accountId, botId, channel, sessionId, message);
				
		resp.getWriter().print(1);
	}
	
}
