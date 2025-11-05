package com.wayos.servlet;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;

import com.wayos.Configuration;
import com.wayos.servlet.console.ConsoleServlet;
import com.wayos.util.Secure;

import x.org.json.JSONObject;

@SuppressWarnings("serial")
@WebServlet("/register")
public class RegisterServlet extends ConsoleServlet {
	
	class RegisterException extends Exception {
		RegisterException(String message) {
			super(message);
		}
	};
	
	private Secure secure = new Secure();
		
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String status = parseJsonStatus("", "", "");
		
		try {
			
			String email = req.getParameter("email");	
			String language = req.getParameter("language");
			String password = req.getParameter("password");
			String confirmPassword = req.getParameter("confirmPassword");
			
			if (email.isEmpty() || language.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
				
				throw new RegisterException(parseJsonStatus("register", "fail", "A01"));						
			}
			
			JSONObject userProfile = storage().readAsJSONObject(Configuration.USER_PATH + email + ".json");
						
			/**
			 * Check If is new user,
			 * So Register New User
			 */
			if (userProfile != null) {
				
				throw new RegisterException(parseJsonStatus("register", "fail", "A02"));
			}
			
			if (!password.equals(confirmPassword)) {
				
				throw new RegisterException(parseJsonStatus("register", "fail", "A03"));						
			}
			
			if (!isValidPassword(password)) {
				
				throw new RegisterException(parseJsonStatus("register", "fail", "A04"));
			}
						
			status = register(req, language, email, password);
						
		} catch (RegisterException e) {
			
			status = e.getMessage();
		
		} catch (Exception e) {
	    	
			throw new RuntimeException(e);
	    	
	    }
		
		resp = setRespHead(resp, Configuration.domain(req));
		resp.getWriter().write(status);			
	}
	
	private boolean isValidPassword(String password) {
		
	    if (password.length() < 8) {
	        return false;
	    }
	    
	    Pattern UpperCasePatten = Pattern.compile("[A-Z]");
	    Pattern lowerCasePatten = Pattern.compile("[a-z]");
	    Pattern digitCasePatten = Pattern.compile("[0-9]");
		
	    if (!UpperCasePatten.matcher(password).find()) {
	        return false;
	    }
	    if (!lowerCasePatten.matcher(password).find()) {
	        return false;
	    }
	    if (!digitCasePatten.matcher(password).find()) {
	        return false;
	    }
	    
		return true;	
	}		
	
	private String generateId() {
		
		return Long.toString(System.currentTimeMillis());
	}
	

	private String register(HttpServletRequest req, String language, String email, String password) {
		
		JSONObject account = new JSONObject();
		
		Date now = now();
		
		String botId = generateId();
		
		/**
		 * Account's Information
		 */
		account.put("Email", email);
		account.put("EncryptedPassword", secure.encryptPassword(password));
		account.put("Created Date", now());
		
		/**
		 * Save as JSON Text File
		 */
		storage().write(account.toString(), Configuration.USER_PATH + email + ".json");
		
		/**
		 * Create Default Chatbot
		 */
		ResourceBundle bundle = ResourceBundle.getBundle("com.wayos.i18n.text", req.getLocale());
				
		String contextName = email + "/" + botId;
		
		/**
		 * Try to clone from Template
		 */
		try {
			
			Map<String, String> propertyMap = new HashMap<>();
			
			/**
			 * Account's Information
			 */
			propertyMap.put("createdDate", formatDate(now));
			//propertyMap.put("accountId", accountId);
			//propertyMap.put("status", "disable");
			
			/*
			propertyMap.put("title", title);
			propertyMap.put("language", language);
			propertyMap.put("greeting", bundle.getString("chatbox.greeting.message"));
			propertyMap.put("unknown", bundle.getString("chatbox.unknown.message"));
			*/
			String templateContent = IOUtils.toString(getClass().getResourceAsStream("/template/helloworld.context"), 
					StandardCharsets.UTF_8.name());

			
			addNewBot(templateContent, contextName, propertyMap);
		
		} catch (Exception e) {
			
			throw new RuntimeException(e);
			
		}
				
		return parseJsonStatus("Register", setSession(req, email, botId), contextName);
		
	}
	
	private String setSession(HttpServletRequest req, String email, String botId) {
		
		HttpSession session = req.getSession(true);
		
		session.setAttribute("accountId", email);
		session.setAttribute("botId", botId);
		
		return "success";
	}
	
}
