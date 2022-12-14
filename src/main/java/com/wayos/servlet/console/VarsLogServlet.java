package com.wayos.servlet.console;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.wayos.util.ConsoleUtil;

/**
 * Servlet implementation class LogsServlet
 */
@WebServlet("/console/vars/*")
public class VarsLogServlet extends ConsoleServlet {
	
	private static final long serialVersionUID = 1L;
       
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String contextName = contextName(request.getRequestURI(), false);
		
		String [] tokens = contextName.split("/");
		
		String accountId = tokens[0];
		
		String botId = tokens[1];
		
		String dateString = request.getParameter("date");
		
		ConsoleUtil consoleUtil = consoleUtil();
		
		response.setContentType("application/json");
		
		if (dateString==null) {
									
			response.getWriter().print(consoleUtil.dateList(accountId, botId));
			
			return;
		}
		
		response.getWriter().print(consoleUtil.readVarsFromDate(accountId, botId, dateString));
	}
	
}
