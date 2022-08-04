package com.wayos.servlet.console;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.wayos.Context;

@SuppressWarnings("serial")
@WebServlet("/console/props/*")
public class ContextPropertiesServlet extends ConsoleServlet {
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		String contextName = contextName(req.getRequestURI(), false);

		try {
			
			Context context = sessionPool().getContext(contextName);
			
			context.load();

			JSONObject properties = new JSONObject(context.prop());
			
			resp.setContentType("application/json");
			
			resp.setCharacterEncoding("UTF-8");
			
			resp.getWriter().print(properties.toString());
			
		} catch (Exception e) {
			
			throw new RuntimeException(e);
			
		}
	}
	
	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String contextName = contextName(req.getRequestURI(), false);
		
		Enumeration<String> parameterNames = req.getParameterNames();
		
		Map<String, String> propertyMap = new HashMap<>();
		
		String parameterName;
		while (parameterNames.hasMoreElements()) {
			
			parameterName = parameterNames.nextElement();
						
			propertyMap.put(parameterName, req.getParameter(parameterName));
			
		}

		try {
			
			Context context = sessionPool().getContext(contextName);
			
			context.load();
			
			/**
			 * Update properties
			 */
			for (Map.Entry<String, String> entry:propertyMap.entrySet()) {
				
				context.prop(entry.getKey(), entry.getValue());
			}

			context.save();
			
			context.load(); //Reload after save
			
			resp.setContentType("text/plain");

			resp.getWriter().print("success");
			
		} catch (Exception e) {
			
			throw new RuntimeException(e);
			
		}
				
	}
	
}
