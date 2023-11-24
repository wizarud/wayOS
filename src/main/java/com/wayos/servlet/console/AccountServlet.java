package com.wayos.servlet.console;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.wayos.Configuration;
import com.wayos.util.URItoContextResolver;

@SuppressWarnings("serial")
@WebServlet("/console/account/*")
public class AccountServlet extends ConsoleServlet {
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		String accountJSONPath = accountJSONPath(req.getRequestURI());

		resp.setContentType("application/json");
		
		resp.setCharacterEncoding("UTF-8");
		
		JSONObject jsonObject = storage().readAsJSONObject(accountJSONPath);
		
		if (jsonObject==null) {
			
			throw new IllegalArgumentException(accountJSONPath);
			
		}
					
		resp.getWriter().print(jsonObject.toString());
		
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String accountJSONPath = accountJSONPath(req.getRequestURI());
		
		resp.setContentType("application/json");
		
		resp.setCharacterEncoding("UTF-8");
		
		JSONObject jsonObject = storage().readAsJSONObject(accountJSONPath);
		
		if (jsonObject==null) {
			
			jsonObject = new JSONObject();
		}
		
		Map<String, String> propertyMap = propertyMap(req);
		
		if (propertyMap.isEmpty()) throw new IllegalArgumentException("Empty parameters");
		
		/**
		 * Update properties
		 */
		for (Map.Entry<String, String> entry:propertyMap.entrySet()) {
			
			jsonObject.put(entry.getKey(), entry.getValue());
		}
				
		storage().write(jsonObject.toString(), accountJSONPath);
	}

	/**
	 * Remapping to users path
	 * @param requestURI
	 * @return
	 */
	private String accountJSONPath(String requestURI) {
		
		int numSlashs = 4;
		
		if (!super.isRoot()) {
			
			numSlashs += 1;
		}
		
		String [] paths = requestURI.split("/", numSlashs);
		
		String accountId = paths[numSlashs-1];
		
		/**
		 * Extract contextName and mapping to the private location
		 */
		String jsonPath = Configuration.USER_PATH + accountId + ".json";
		
		return jsonPath;
	}

}