package com.wayos.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.wayos.servlet.console.ConsoleServlet;
import com.wayos.util.ConsoleUtil;

/**
 * Servlet implementation class LogsServlet
 */
@SuppressWarnings("serial")
@WebServlet("/logs")
public class TestLogsServlet extends ConsoleServlet {
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String sessionId = request.getParameter("sessionId");
		
		String accountId = request.getParameter("accountId");
		
		String botId = request.getParameter("botId");
		
		if (accountId==null || botId==null) {
			
			accountId = "103014451870896";
			botId = "Michael-Alfdhgjbcibgd-Rosenthalsen";
		
		}
		
		String message = request.getParameter("message");
		
		ConsoleUtil consoleUtil = new ConsoleUtil(storage());
		
		if (sessionId==null) {
			
			response.setContentType("application/json");
			
			response.getWriter().print(consoleUtil.dateList(accountId, botId));
			
			return;
		}
		
		if (message==null) {
			
			response.getWriter().print(sessionId + " was indexed at " + consoleUtil.nowString());
			
			return;
		}
		
		consoleUtil.appendLogVars(null, accountId, botId, "test", sessionId, message, "|");
		
		response.setContentType("application/json");
		
		response.getWriter().print(consoleUtil.readLogVarsFromDate(accountId, botId, consoleUtil.nowString()));
	}

}
