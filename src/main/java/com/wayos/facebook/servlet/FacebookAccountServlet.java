package com.wayos.facebook.servlet;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.FacebookClient.AccessToken;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.types.User;
import com.wayos.Configuration;
import com.wayos.Context;
import com.wayos.servlet.console.ConsoleServlet;

@SuppressWarnings("serial")
@WebServlet("/fbSignIn")
public class FacebookAccountServlet extends ConsoleServlet {
		
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String act = req.getParameter("act");
		String language = req.getParameter("language");
						
		String status = parseJsonStatus("", "", "");
		
		try {
			
			String accountId, email, name, imageURL;
			
			FacebookClient facebookClient = new DefaultFacebookClient(act, Configuration.facebook_appSecret, Version.LATEST);
			User user = facebookClient.fetchObject("me", User.class);
									
			accountId = user.getId();				
			
			User me = facebookClient.fetchObject(user.getId(), User.class, Parameter.with("fields", "name, email, hometown, picture.width(512).height(512)"));
			name = me.getName();
			email = me.getEmail();
			imageURL = me.getPicture().getUrl();
									
			email = email == null || email.trim().isEmpty() ? "" : email;				
			
			AccessToken accessToken = new DefaultFacebookClient(Version.LATEST).obtainExtendedAccessToken(Configuration.facebook_appId, Configuration.facebook_appSecret, act);				
			
			req.getSession().setAttribute("facebookAccessToken", accessToken.getAccessToken());
			
			JSONObject account = storage().readAsJSONObject(Configuration.USER_PATH + accountId + ".json");
						
			/**
			 * Check If is new user,
			 * So Register New User
			 */
			if (account == null) {
				
				status = register(req, language, status, accountId, email, name, imageURL);
								
			}
			
			/**
			 * Do Login process
			 */			
			else {
				
				name = account.getString("Name");
				email = account.getString("Email");
				
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
						botSelectedId = account.getString("eoss_Bot_Selected");						
					} catch (Exception e) {						
						/**
						 * TODO: Query Default selected bot from child objects
						 */
						botSelectedId = "";
					}
				
				} 
				
				String contextName = Configuration.contextHome + accountId + "/" + botSelectedId + Context.SUFFIX;
				
				status = parseJsonStatus("SignIn", setSession(req, accountId, botSelectedId), contextName);
				
			}
			
		} catch (Exception e) {
	    	
			throw new RuntimeException(e);
	    	
	    }
		
		resp = setRespHead(resp, Configuration.domain);
		resp.getWriter().write(status);			
	}
	
	private String generateBotId() {
		
		return Long.toString(System.currentTimeMillis());
	}
	

	private String register(HttpServletRequest req, String language, String status, String accountId, String email, String name, String imageURL) {
		
		JSONObject account = new JSONObject();
		
		Date now = now();
		
		/**
		 * Account's Information
		 */
		account.put("Name", name);		
		account.put("Id", accountId);		
		account.put("Email", email);
		account.put("Created Date", now());
		
		/**
		 * Save as JSON Text File
		 */
		storage().write(account.toString(), Configuration.USER_PATH + accountId + ".json");
		
		/**
		 * Create Default Chatbot
		 */
		ResourceBundle bundle = ResourceBundle.getBundle("com.wayos.i18n.text", req.getLocale());
		
		//String botId = generateBotId();
		String botId = name.replaceAll("\\s+", "-");
		
		String contextName = accountId + "/" + botId;
		
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
				
		/*
		 * Download FB Image profile
		 */
		if (imageURL!=null) {
			
			try {
				
				storage().write(new URL(imageURL).openStream(), Configuration.USER_PATH + accountId + ".PNG");
								
			} catch (Exception e) {
				
				throw new RuntimeException(e);
			}	
		}

		status = parseJsonStatus("Register", setSession(req, accountId, botId), contextName);
		
		return status;
	}
	
	private String setSession(HttpServletRequest req, String accountId, String botId) {
		
		HttpSession session = req.getSession(true);
		
		session.setAttribute("accountId", accountId);
		session.setAttribute("botId", botId);
		
		return "success";
	}
	
}
