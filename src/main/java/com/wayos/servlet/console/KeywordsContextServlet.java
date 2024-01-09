package com.wayos.servlet.console;

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.wayos.Context;
import com.wayos.Hook;
import com.wayos.Node;

import x.org.json.JSONArray;

/**
 * Property / Session Variables Management Servlet
 * @author eoss-th
 *
 */
@SuppressWarnings("serial")
@WebServlet("/console/keywords/*")
public class KeywordsContextServlet extends ConsoleServlet {
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		/**
		 * TODO: Validate owner accountId
		 */
		String contextName = contextName(req.getRequestURI(), false);

		Context context = sessionPool().getContext(contextName);

		try {
			
			context.load();
			
			/**
			 * Load session variables from json file
			 */
			Set<String> keywords = new TreeSet<>();
			
			String keyword;
			for (Node node:context.nodeList()) {
				
				keyword = "";
				
				for (Hook hook:node.hookList()) {
					
					if (hook.text.startsWith("@") ||
						hook.text.trim().isEmpty() ||						
						hook.text.startsWith("gt") ||
						hook.text.startsWith("lt") ||
						hook.text.startsWith("#") //Skip variable too
						) {
						continue;//Skip it
					}
					keyword += hook.text + " ";
				}
				
				keyword = keyword.trim();
				
				if (keyword.isEmpty()) continue;
				
				keywords.add(keyword);
			}
					
	        resp.setContentType("application/json");
	        resp.setCharacterEncoding("UTF-8");
	        resp.getWriter().write(new JSONArray(keywords).toString());
			
			return;			
			
		} catch (Exception e) {
			
			throw new RuntimeException(e);
		}
				
	}
	
}
