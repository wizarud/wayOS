package com.wayos.facebook.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@SuppressWarnings("serial")
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		try {
	
			HttpSession httpSession = req.getSession();
			
			httpSession.removeAttribute("accountId");
			httpSession.removeAttribute("botId");

			httpSession.invalidate();
			
			
			resp.getWriter().write("success");
			
		} catch (Exception e) {
			
			resp.getWriter().write("fail");
		}
	}
	
}
