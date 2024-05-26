package com.wayos.connector.rest;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.wayos.Configuration;
import com.wayos.Session;
import com.wayos.connector.http.HttpRequestObject;

public class RestHttpRequestObject extends HttpRequestObject {
	
	public RestHttpRequestObject(HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
				
        if (ServletFileUpload.isMultipartContent(request)) {
        	
    		if (sessionId==null) throw new IllegalArgumentException("Null Session Id");
    		
    		if (message==null || message.isEmpty()) {
    			
        		message = uploadedPaths(request).trim();
        		
    		} else {
    			    			
    			message += " " + uploadedPaths(request).trim();
    			
    		}
    		        	
        }
        
        messageObject.attr("channel", "web");
    }
		
	private String uploadedPaths(HttpServletRequest request) {
		
		StringBuilder lines = new StringBuilder();
		
		String [] tokens = contextName.split("/");
		
		String accountId = tokens[0];
		String botId = tokens[1];
		
		try {
			
			DiskFileItemFactory factory = new DiskFileItemFactory();

			// Set factory constraints
			int yourMaxMemorySize = 1048576;
			File yourTempDirectory = new File(System.getenv("storagePath") + request.getServletContext().getContextPath());
			factory.setSizeThreshold(yourMaxMemorySize);
			factory.setRepository(yourTempDirectory);

			// Create a new file upload handler
			ServletFileUpload upload = new ServletFileUpload(factory);
			
			List<FileItem> items = upload.parseRequest(request);			
			Iterator<FileItem> iterator = items.iterator();
			
			String fileName;
			FileItem item;
			while (iterator.hasNext()) {
				
				item = iterator.next();
				
				if (item.isFormField()) {
										
					lines.append(item.getString());
					
					lines.append(" ");
					
					continue;
				}
				
				fileName = URLEncoder.encode(item.getName(), StandardCharsets.UTF_8.toString());
				
				String toPath = "public/" + accountId + "/" + fileName;
				
				storage().write(item.getInputStream(), toPath);
				
				String contextRoot = request.getContextPath();
				
				lines.append(Configuration.domain + contextRoot + "/public/" + accountId + "/" + fileName);
				
				lines.append(" ");
			}
			
		} catch (Exception e) {
			
			throw new RuntimeException(e);
		}
		
		return lines.toString().trim();
	}	
	
	@Override
	public void prepare(Session session) {
		
		/**
		 * Call super if this sessionId is registered admin
		 */
		super.prepare(session);
		
        session.vars("#s_fullName",  "Guest");
		
	}
	
	
}
