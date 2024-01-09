package com.wayos.servlet.console;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.wayos.Configuration;

import x.org.json.JSONObject;

@SuppressWarnings("serial")
@WebServlet("/console/line/*")
public class LINEConfigServlet extends ConsoleServlet {
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		String contextName = contextName(req.getRequestURI(), false);
		
		String jsonPath = new Configuration(contextName).linePath();

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
		
		String jsonPath = new Configuration(contextName).linePath();
		
		Enumeration<String> parameterNames = req.getParameterNames();
		
		Map<String, String> propertyMap = new HashMap<>();
		
		String parameterName;
		while (parameterNames.hasMoreElements()) {
			
			parameterName = parameterNames.nextElement();
						
			propertyMap.put(parameterName, req.getParameter(parameterName));
			
		}

		try {
			
			JSONObject jsonObject = storage().readAsJSONObject(jsonPath);
			
			if (jsonObject==null) {
				
				jsonObject = new JSONObject();
			}
			
			/**
			 * Update properties
			 */
			for (Map.Entry<String, String> entry:propertyMap.entrySet()) {
				
				jsonObject.put(entry.getKey(), entry.getValue());
			}
			
			storage().write(jsonObject.toString(), jsonPath);
			
			resp.setContentType("text/plain");

			resp.getWriter().print("success");
			
		} catch (Exception e) {
			
			throw new RuntimeException(e);
			
		}
				
	}
	
}