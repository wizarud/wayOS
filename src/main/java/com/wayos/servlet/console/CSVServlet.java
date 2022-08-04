package com.wayos.servlet.console;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.wayos.Configuration;
import com.wayos.Context;
import com.wayos.util.CSVWrapper;

@SuppressWarnings("serial")
@WebServlet("/console/csv/*")
public class CSVServlet extends ConsoleServlet {
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String contextName = contextName(req.getRequestURI(), false);
		
		String type = req.getParameter("type");
		
		/**
		 * Load TSV for Example catalog.tsv
		 */
		if (type!=null && type.endsWith(".tsv")) {
			
		    try {
		    	
	    		String catalogTSVPath = Configuration.PRIVATE_PATH + contextName + ".catalog.tsv";
		    	
				storage().write(catalogTSVPath, resp.getOutputStream());
				
		    } catch (Exception e) {
		    	
		    	throw new RuntimeException(e);
		    }
			
			return;
		}
				
		try {
			
			Context context = sessionPool().getContext(contextName);
			
			context.load();
			
			resp.setContentType("text/plain");
			
			resp.setCharacterEncoding("UTF-8");
			
			resp.setHeader("Content-disposition", "attachment; filename=" + context.prop("title") + ".tsv");
			
			resp.getWriter().write(new CSVWrapper(context, "\t").toString());
			
		} catch (Exception e) {
			
			throw new RuntimeException(e);
		}
	}
	
}
