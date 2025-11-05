package com.wayos.servlet.console;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.wayos.Application;
import com.wayos.Configuration;
import com.wayos.Context;
import com.wayos.PathStorage;
import com.wayos.connector.SessionPool;
import com.wayos.pusher.PusherUtil;
import com.wayos.util.ConsoleUtil;

import x.org.json.JSONObject;

@SuppressWarnings("serial")
public class ConsoleServlet extends HttpServlet {

	public Date now() {
		
		return new Date();
	}
	
	/**
	 * For Registration new context
	 * @param date
	 * @return
	 */
	public String formatDate(Date date) {
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MMM-dd hh:mm:ss", Locale.US);
		//TimeZone.setDefault(TimeZone.getTimeZone("GMT+7"));
		
		return dateFormat.format(date);
		
	}
	
	protected final String parseJsonStatus(String id, String status, String message) {
		
		Map<String, String> data = new HashMap<String, String>();
		data.put("id", id);
		data.put("status", status);
		data.put("message", message);
		
		return new JSONObject(data).toString();
	}
	
	protected final HttpServletResponse setRespHead(HttpServletResponse resp, String domain) {
		
		/**
		 * For empty domain
		 */
		/*
		if (domain.isEmpty()) {
			domain = "*";
		}
		*/
		
		resp.setHeader("Access-Control-Allow-Origin", domain);
		resp.setContentType("application/json");
		resp.setCharacterEncoding("UTF-8");
		
		return resp;
	}
		
	protected SessionPool sessionPool() {
		
		return Application.instance().get(SessionPool.class);
	}
	
	protected final PathStorage storage() {
		
		return Application.instance().get(PathStorage.class);		
	}
	
	protected final ConsoleUtil consoleUtil() {
		
		return Application.instance().get(ConsoleUtil.class);
	}
	
	protected final PusherUtil pusherUtil() {
		
		return Application.instance().get(PusherUtil.class);
	}

	protected final void addNewBot(String jsonContext, String contextName, Map<String, String> propertyMap) throws Exception {
		
		PathStorage storage = storage();
		
		Configuration configuration = new Configuration(contextName);
		
		System.out.println("save.. to " + configuration.contextPath());
		
		storage.write(jsonContext, configuration.contextPath());
		
		Context context = sessionPool().getContext(contextName);
				
		for (Map.Entry<String, String> entry:propertyMap.entrySet()) {
			
			context.prop(entry.getKey(), entry.getValue());
		}
				
		Map<String, String> chatBoxAttributes = new HashMap<>();
		chatBoxAttributes.put("borderColor", "#");
		chatBoxAttributes.put("bgColor", "#");
		chatBoxAttributes.put("textColor", "#");
		chatBoxAttributes.put("cssId", "default1");
		
		context.attr("chatbox", chatBoxAttributes);
		
		context.save();			
		
	}
	
	protected final boolean isRoot() {
		
		return getServletContext().getContextPath().isEmpty();
	}
		
	protected final String contextName(String requestURI, boolean isPublic) {
		
		/**
		 * Call from URI patterns 
		 * Public: /x/* 
		 * Console: /x/y/*
		 */
		int numSlashs = isPublic ? 3 : 4;
		
		/**
		 * Plus one for contextRoot, Ex <domain>/<contextRoot>/x/*
		 */
		if (!isRoot()) {
			numSlashs += 1;
		}
		
		String [] paths = requestURI.split("/", numSlashs);
		
		String contextName = paths[numSlashs - 1];
		
		return	contextName;
	}
	
	protected final Map<String, String> propertyMap(HttpServletRequest req) {
		
		Enumeration<String> parameterNames = req.getParameterNames();
		
		Map<String, String> propertyMap = new HashMap<>();
		
		String parameterName;
		
		while (parameterNames.hasMoreElements()) {
			
			parameterName = parameterNames.nextElement();
						
			propertyMap.put(parameterName, req.getParameter(parameterName));
			
		}
		
		return propertyMap;
	}

}
