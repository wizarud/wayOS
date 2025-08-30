package com.wayos.servlet.console;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.wayos.Application;
import com.wayos.Configuration;
import com.wayos.PathStorage;
import com.wayos.util.MessageTimer;
import com.wayos.util.MessageTimerTask;

import x.org.json.JSONObject;

@SuppressWarnings("serial")
@WebServlet("/console/task/*")
public class TaskConfigServlet extends ConsoleServlet {
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		String contextName = contextName(req.getRequestURI(), false);
		
		String jsonPath = new Configuration(contextName).silentPath();

		try {
			
			resp.setContentType("application/json");
			
			resp.setCharacterEncoding("UTF-8");
			
			JSONObject jsonObject = storage().readAsJSONObject(jsonPath);
			
			if (jsonObject==null) {
				
				resp.getWriter().print("{}");
				return;
			}
						
			resp.getWriter().print(jsonObject.toString());
			
		} catch (Exception e) {
			
			throw new RuntimeException(e);
			
		}
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String contextName = contextName(req.getRequestURI(), false);
		
		String jsonPath = new Configuration(contextName).silentPath();
		
		Enumeration<String> parameterNames = req.getParameterNames();
		
		Map<String, String> propertyMap = new HashMap<>();
		
		String parameterName;
		
		while (parameterNames.hasMoreElements()) {
			
			parameterName = parameterNames.nextElement();
						
			propertyMap.put(parameterName, req.getParameter(parameterName));
			
		}

		try {
			
			MessageTimer silentFire = Application.instance().get(MessageTimer.class);
			
			/**
			 * Delete if parameters (message and interval) are empty
			 */
			if (propertyMap.isEmpty()) {
				
				silentFire.cancel(contextName);
				
				PathStorage storage = Application.instance().get(PathStorage.class);
				
				storage.delete(jsonPath);
				
				resp.setContentType("text/plain");

				resp.getWriter().print("success");
				
				return;
			}
			
			JSONObject taskObj = storage().readAsJSONObject(jsonPath);
			
			if (taskObj==null) {
				
				taskObj = new JSONObject();
			}
			
			/**
			 * Update properties
			 */
			for (Map.Entry<String, String> entry:propertyMap.entrySet()) {
				
				taskObj.put(entry.getKey(), entry.getValue());
			}
			
			/**
			 * Reschedule
			 */
			
			silentFire.register(MessageTimerTask.build(contextName, taskObj));
						
			resp.setContentType("text/plain");

			resp.getWriter().print("success");
			
		} catch (Exception e) {
			
			throw new RuntimeException(e);
			
		}
				
	}
	
}