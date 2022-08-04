package com.wayos.servlet.console;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

@SuppressWarnings("serial")
@WebServlet("/console/account/*")
public class AccountServlet extends ConsoleServlet {
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		String jsonPath = privateJSONPath(req.getRequestURI());

		resp.setContentType("application/json");
		
		resp.setCharacterEncoding("UTF-8");
		
		JSONObject jsonObject = storage().readAsJSONObject(jsonPath);
		
		if (jsonObject==null) {
			
			throw new IllegalArgumentException(jsonPath);
			
		}
					
		resp.getWriter().print(jsonObject.toString());
	}

	/**
	 * Remapping to users path
	 * @param requestURI
	 * @return
	 */
	private String privateJSONPath(String requestURI) {
		
		String [] paths = requestURI.split("/", 4);
		
		String accountId = paths[3];
		
		/**
		 * Extract contextName and mapping to the private location
		 */
		String jsonPath = "users/" + accountId + ".json";
		
		return jsonPath;
	}

}