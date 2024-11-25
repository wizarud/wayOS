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
		
		String yearAndMonth = request.getParameter("yearAndMonth");
		
		ConsoleUtil consoleUtil = consoleUtil();
		
		response.setCharacterEncoding("UTF-8");
		
		response.setContentType("application/json");
		
		if (dateString==null) {
			
			/**
			 * Year and Month List by logs datetime
			 */
			if (yearAndMonth==null) {
				
				response.getWriter().print(consoleUtil.logsGroupAsYearAndMonth(accountId, botId));
				
				return;
				
			}

			response.getWriter().print(consoleUtil.dateList(accountId, botId, yearAndMonth));
			
			return;
		}
		
		/**
		 * TODO: mark as read!
		 */
		//consoleUtil().removeReadLogVars(accountId, botId, dateString);
		
		response.getWriter().print(consoleUtil.readLogVarsFromDate(accountId, botId, dateString));
	}
	
}
