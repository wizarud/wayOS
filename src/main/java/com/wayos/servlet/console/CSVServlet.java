package com.wayos.servlet.console;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

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
		
		resp.setContentType("text/plain");
		
		resp.setCharacterEncoding("UTF-8");
		
		String contextName = contextName(req.getRequestURI(), false);
		
		String type = req.getParameter("type");
		
		/**
		 * Load TSV for Example catalog.tsv
		 */
		if (type!=null && (type.endsWith(".tsv") || type.endsWith(".txt"))) {
			
		    try {
		    	
	    		String contentPath = Configuration.PRIVATE_PATH + contextName + "." + type;
	    		
	    		if (!storage().exists(contentPath)) throw new IllegalArgumentException(contentPath + " not exists");
	    		
				storage().write(contentPath, resp.getOutputStream());
				
		    } catch (Exception e) {
		    	
		    	
		    	/**
		    	 * CHAI , For first time creation
		    	 */
		    	if (type.equals("chai.tsv")) {
		    		
		    		try {
		    			
						Context context = sessionPool().getContext(contextName);
						
						context.load();
						
						resp.getWriter().write(new CSVWrapper(context, "\t").toString());
						
		    		} catch (Exception ee) {
		    			
		    			resp.getWriter().print(ee);
		    			
		    		}
		    		
		    		return;
		    	}
		    	
		    	/**
		    	 * Sample Quiz
		    	 */
		    	if (type.equals("qa.tsv")) {
		    		
		    		try {
						Context context = sessionPool().getContext(contextName);
						
						context.load();
						
						String language = context.prop("language");
						if (language==null) {
							language = "en";
						}
						
						Locale locale = new Locale(language);
			    		ResourceBundle bundle = ResourceBundle.getBundle("com.wayos.i18n.text", locale);
			    		
			    		resp.getWriter().print(bundle.getString("quiz.sample"));
		    			
		    		} catch (Exception ee) {
		    			
		    			resp.getWriter().print(ee);
		    			
		    		}
		    		
		    		return;
		    	}
		    	
		    	/**
		    	 * Sample Form
		    	 */
		    	if (type.equals("form.tsv")) {
		    		
		    		try {
						Context context = sessionPool().getContext(contextName);
						
						context.load();
						
						String language = context.prop("language");
						if (language==null) {
							language = "en";
						}
						
						Locale locale = new Locale(language);
			    		ResourceBundle bundle = ResourceBundle.getBundle("com.wayos.i18n.text", locale);
			    		
			    		resp.getWriter().print(bundle.getString("form.sample"));
		    			
		    		} catch (Exception ee) {
		    			
		    			resp.getWriter().print(ee);
		    			
		    		}
		    		
		    		return;
		    	}		  
		    	
		    	/**
		    	 * Sample FAQ
		    	 */
		    	if (type.equals("faq.tsv")) {
		    		
		    		try {
						Context context = sessionPool().getContext(contextName);
						
						context.load();
						
						String language = context.prop("language");
						if (language==null) {
							language = "en";
						}
						
						Locale locale = new Locale(language);
			    		ResourceBundle bundle = ResourceBundle.getBundle("com.wayos.i18n.text", locale);
			    		
			    		resp.getWriter().print(bundle.getString("faq.sample"));
		    			
		    		} catch (Exception ee) {
		    			
		    			resp.getWriter().print(ee);
		    			
		    		}
		    		
		    		return;
		    	}
		    	
		    	/**
		    	 * Presentation
		    	 */
		    	if (type.equals("pt.tsv")) {
		    		
		    		try {
						Context context = sessionPool().getContext(contextName);
						
						context.load();
						
						String language = context.prop("language");
						if (language==null) {
							language = "en";
						}
						
						Locale locale = new Locale(language);
			    		ResourceBundle bundle = ResourceBundle.getBundle("com.wayos.i18n.text", locale);
			    		
			    		resp.getWriter().print(bundle.getString("pt.sample"));
		    			
		    		} catch (Exception ee) {
		    			
		    			resp.getWriter().print(ee);
		    			
		    		}
		    		
		    		return;
		    	}	
		    	
		    	/**
		    	 * Way
		    	 */
		    	if (type.equals("way.txt")) {
		    		
		    		try {
						Context context = sessionPool().getContext(contextName);
						
						context.load();
						
						String language = context.prop("language");
						if (language==null) {
							language = "en";
						}
						
						Locale locale = new Locale(language);
			    		ResourceBundle bundle = ResourceBundle.getBundle("com.wayos.i18n.text", locale);
			    		
			    		resp.getWriter().print(bundle.getString("way.sample"));
		    			
		    		} catch (Exception ee) {
		    			
		    			resp.getWriter().print(ee);
		    			
		    		}
		    		
		    		return;
		    	}		    	
		    	
		    }
			
			return;
			
		}
		
		/**
		 * Export TSV
		 */
		
		try {
			
			Context context = sessionPool().getContext(contextName);
			
			String botId = contextName.split("/")[1];
			
			context.load();
			
			resp.setHeader("Content-disposition", "attachment; filename=" + botId + ".tsv");
			
			resp.getWriter().write(new CSVWrapper(context, "\t").toString());
			
		} catch (Exception e) {
			
			throw new RuntimeException(e);
		}
	}
	
}
