package com.wayos.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.wayos.Configuration;
import com.wayos.Context;
import com.wayos.servlet.console.ConsoleServlet;
import com.wayos.util.Secure;

import x.org.json.JSONObject;

@SuppressWarnings("serial")
@WebServlet("/signIn")
public class SignInServlet extends ConsoleServlet {

	class InvalidPasswordException extends Exception {
		InvalidPasswordException(String message) {
			super(message);
		}
	};

	private Secure secure = new Secure();

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		String status = parseJsonStatus("", "", "");

		try {
			
			String email = req.getParameter("email");

			String password = req.getParameter("password");
			
			JSONObject userProfile = storage().readAsJSONObject(Configuration.USER_PATH + email + ".json");

			if (userProfile==null) {

				throw new InvalidPasswordException(parseJsonStatus("SignIn", "fail", "Invalid Email or Password"));
			}
			
			String encryptedPassword = userProfile.getString("EncryptedPassword");

			if (!secure.encryptPassword(password).equals(encryptedPassword)) {

				throw new InvalidPasswordException(parseJsonStatus("SignIn", "fail", "Invalid Email or Password"));
			}

			/**
			 * Find botId for landing page
			 */
			String botSelectedId = null;
			Cookie [] cookies = req.getCookies();
			for (Cookie c:cookies) {
				if (c.getName().equals("eoss_Bot_Selected")) {
					botSelectedId = c.getValue();
					break;
				}
			}

			if (botSelectedId==null) {

				try {
					botSelectedId = userProfile.getString("eoss_Bot_Selected");						
				} catch (Exception e) {						
					/**
					 * TODO: Query Default selected bot from child objects
					 */
					botSelectedId = "";
				}

			} 

			String contextName = Configuration.contextHome + email + "/" + botSelectedId + Context.SUFFIX;

			status = parseJsonStatus("SignIn", setSession(req, email, botSelectedId), contextName);

		} catch (InvalidPasswordException e) {

			status = e.getMessage();

		} catch (Exception e) {

			throw new RuntimeException(e);

		}

		resp = setRespHead(resp, Configuration.domain(req));
		resp.getWriter().write(status);			
	}

	private String setSession(HttpServletRequest req, String email, String botId) {
		
		HttpSession session = req.getSession(true);
		
		session.setAttribute("accountId", email);
		session.setAttribute("botId", botId);
		
		return "success";
	}

}
