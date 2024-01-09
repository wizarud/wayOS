package com.wayos.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.wayos.Context;
import com.wayos.servlet.console.ConsoleServlet;

import x.org.json.JSONObject;

@SuppressWarnings("serial")
@WebServlet("/x/*")
public class ShowcaseServlet extends ConsoleServlet {
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		String contextName = contextName(req.getRequestURI(), true);

		String [] tokens = contextName.split("/");

		String accountId = tokens[0];
		String botId = tokens[1];
		
		req.setAttribute("accountId", accountId);
		req.setAttribute("botId", botId);
		
		try {
			
			Context context = sessionPool().getContext(contextName);
			
			context.load();//TODO: Do we need to load again?
			
			/**
			* filter only title, desc, borderColor & loadingGif
			*/
			JSONObject properties = new JSONObject(context.prop());
			properties = new JSONObject(context.prop());
			properties.remove("greeting");
			properties.remove("silent");
			properties.remove("unknown");
			
			req.setAttribute("props", properties);
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException(req.getRequestURI() + "=>" + contextName);
			
		}
				
		req.getRequestDispatcher("/play.jsp").forward(req, resp);
	}
	
}
