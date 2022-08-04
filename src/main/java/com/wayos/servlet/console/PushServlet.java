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
						
		String sessionId = req.getParameter("sessionId");
		
		//Broadcast Message <Call from dashboard>
		if (sessionId==null || sessionId.trim().isEmpty()) {
			/**
			 * Parse keyword or just push message
			 */
			if (!keyword.isEmpty()) //Parse keywords
				resp.getWriter().print(pusherUtil().parse(accountId, botId, keyword + " " + message).size());
			else //Or just push message
				resp.getWriter().print(pusherUtil().push(accountId, botId, message).size());
			return;
			
		}
						
		int reaches;
		/**
		 * Parse keyword or just push message
		 */
		if (!keyword.isEmpty()) //Parse keywords
			reaches = pusherUtil().parse(accountId, botId, sessionId, keyword + " " + message).size();
		else //Or just push message
			reaches = pusherUtil().push(accountId, botId, sessionId, message).size();
		
		/**
		 * Error Happens!
		 */
		if (reaches==0) {
			resp.getWriter().print(sessionId + ":" + reaches);
			return;
		}
		
		resp.getWriter().print(reaches);
	}
	
}
