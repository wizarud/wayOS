package com.wayos.servlet;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.wayos.Configuration;
import com.wayos.servlet.console.ConsoleServlet;

/**
 * Serve File in CONTEXT_HOME as Media content
 * 
 * @author Wisarut Srisawet
 *
 */
@SuppressWarnings("serial")
@WebServlet(urlPatterns = {"/public/*", "/p/*"})
public class PublicStorageServlet extends ConsoleServlet {
	
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		String requestURI = req.getRequestURI();
		
		String [] paths;
		String fileName;		
		
		if (super.isRoot()) {
			
			paths = requestURI.split("/", 3);
			fileName = paths[2];
			
		} else {
			
			paths = requestURI.split("/", 4);
			fileName = paths[3];
			
		}

	    String image = req.getParameter("image");
	    
	    if (image!=null) {
	    	
    		resp.setHeader("Content-Type", "image/" + image);
    		
	    } else if (!fileName.contains(".")) {
	    	
	    	/**
	    	 * Default is PNG
	    	 */
    		resp.setHeader("Content-Type", "image/png"); 
    		
	    }

	    /*
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        */
        
        if (storage().exists(Configuration.PUBLIC_PATH + fileName)) {
	    	
		    storage().serve(Configuration.PUBLIC_PATH + fileName, resp);
	    }
	    
	}
	
}