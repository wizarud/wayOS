package com.wayos.servlet.console;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.wayos.Context;

import x.org.json.JSONArray;
import x.org.json.JSONObject;

@SuppressWarnings("serial")
@WebServlet("/console/unread/*")
public class UnReadServlet extends ConsoleServlet {
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		resp.setContentType("text/plain");
		
		String dateFilter = req.getParameter("date");
		
		String contextName = contextName(req.getRequestURI(), false);
		
		String [] tokens = contextName.split("/");
		
		String accountId = tokens[0];
		
		String botId = tokens[1];
		
		int count;
		
		if (dateFilter!=null) {
			
			count = consoleUtil().unreadLogVarsAtDate(accountId, botId, dateFilter);
			
		} else {
			
			count = consoleUtil().allUnreadLogVarsCount(accountId, botId);
			
		}		
		
		resp.getWriter().print(count);

	}
	
	/*
	@Override	
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		resp.setContentType("text/plain");
		
		String targetDate = req.getParameter("date");
		
		String contextName = contextName(req.getRequestURI(), false);
		
		String [] tokens = contextName.split("/");
		
		String accountId = tokens[0];
		
		String botId = tokens[1];
		
		if (targetDate!=null) {
			
			consoleUtil().removeReadLogVars(accountId, botId, targetDate);
			
		} 
		
		resp.getWriter().print("success");
				
	}
	*/
	
}
