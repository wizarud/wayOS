package com.wayos.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.wayos.Context;
import com.wayos.servlet.console.ConsoleServlet;

@SuppressWarnings("serial")
@WebServlet("/props/*")
public class ShowcasePropertiesServlet extends ConsoleServlet {
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		String contextName = contextName(req.getRequestURI(), true);

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
	
}
