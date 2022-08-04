package com.wayos.servlet.console;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;

import com.wayos.Configuration;
import com.wayos.Context;

@SuppressWarnings("serial")
@WebServlet("/console/context/*")
public class ContextServlet extends ConsoleServlet {
	
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		String requestURI = req.getRequestURI();
		
		if (requestURI.endsWith("/")) {
			
			requestURI = requestURI.substring(0, requestURI.lastIndexOf("/"));
			
			String [] paths = requestURI.split("/");
			
			String accountId = paths[paths.length-1];
			
			String resourcePath = Configuration.LIB_PATH + accountId + "/";
			
			String suffix = ".context";		
			
			resp.setContentType("application/json");
			
			List<String> objectList = storage().listObjectsWithPrefix(resourcePath);
			
			JSONArray array = new JSONArray();
			
			String contextName;
			String botId;
			for (String object:objectList) {
				
				if (suffix!=null && !object.endsWith(suffix)) continue;
				
				if (object.equals(resourcePath)) continue;
				
				contextName = object.substring(Configuration.LIB_PATH.length(), object.lastIndexOf(".context"));
				botId = contextName.split("/")[1];
				
				array.put(botId);
			}
			
			resp.getWriter().print(array.toString());
			
			return;
		}
		
		String contextName = contextName(requestURI, false);
		
		Configuration configuration = new Configuration(contextName);
		
	    storage().serve(configuration.contextPath(), resp);
	}
	
	/**
	 * For saving (Updating) context file (Single text from Designer)
	 */
	@Override
	public void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		String requestURI = req.getRequestURI();
		String contextName = contextName(requestURI, false);
		
		Configuration configuration = new Configuration(contextName);
		
		storage().write(req.getInputStream(), configuration.contextPath());
		
		Context context = sessionPool().getContext(contextName);
		try {
			context.load();
		} catch (Exception contextException) {
			throw new RuntimeException(contextException);
		}
		
	}

	/**
	 * For add new bot from dashboard
	 */
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		String requestURI = req.getRequestURI();
		String contextName = contextName(requestURI, false);
		
		String [] tokens = contextName.split("/");
		String accountId = tokens[0];
		String botId = tokens[1];
		
		botId = botId.replaceAll("\\s+", "-");
		botId = botId.replace("%20", "-");
		
		contextName = accountId + "/" + botId;
		
		Configuration configuration = new Configuration(contextName);
		
		/**
		 * Check existing bot
		 */
		if (storage().readAsJSONObject(configuration.contextPath())!=null) {
			
			resp.getWriter().print(super.parseJsonStatus("addNewBot", "error", "Duplicated " + botId));
			return;
		}
				
		Map<String, String> propertyMap = new HashMap<>();
		
		propertyMap.put("created Date", formatDate(now()));
		//propertyMap.put("accountId", accountId);
		//propertyMap.put("status", "disable");
		
		propertyMap.put("language", req.getParameter("language"));
		propertyMap.put("title", req.getParameter("title"));
		
		/**
		 * TODO: Should not override forwarding value (, <@nextId>)
		 */
		//propertyMap.put("greeting", req.getParameter("greeting"));
		//propertyMap.put("unknown", req.getParameter("unknown"));
		
		String templateContent;
		
		String template = req.getParameter("template");
		
		if (template!=null) {
			
			templateContent = storage().readAsJSONObject(new Configuration(template).contextPath()).toString();
			
		} else {
			
			templateContent = IOUtils.toString(getClass().getResourceAsStream("/template/DEFAULT.context"), 
					StandardCharsets.UTF_8.name());
		}
		
		try {
			
			addNewBot(templateContent, contextName, propertyMap);
			
		} catch (Exception e1) {
			
			resp.getWriter().print(super.parseJsonStatus("addNewBot", "error", e1.getClass().getName() + ":" + req.getParameter("template") + ":" + template));
			return;
		}
		
		resp.setContentType("application/json");
		
		resp.getWriter().print(super.parseJsonStatus("addNewBot", "success", "Successfully added " + contextName));
	}
	
}