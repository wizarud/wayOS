package com.wayos.connector.web;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.wayos.Configuration;
import com.wayos.connector.http.HttpRequestObject;

public class WebHttpRequestObject extends HttpRequestObject {
	
	public WebHttpRequestObject(HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
		
        if (request.getContentType().contains("multipart/form-data")) {
        	
    		if (sessionId==null) throw new IllegalArgumentException("Null Session Id");
    		
        	message = uploadedPaths(request).trim();
        	
        }
        
        messageObject.attr("channel", "web");
    }
		
	private String uploadedPaths(HttpServletRequest request) {
		
		StringBuilder lines = new StringBuilder();
		
		String [] tokens = contextName.split("/");
		
		String accountId = tokens[0];
		String botId = tokens[1];
		
		FileItemStream fileItem = null;
		try {
			
			ServletFileUpload upload = new ServletFileUpload();
			
			FileItemIterator iterator = upload.getItemIterator(request);
													
			String fileName;
			while (iterator.hasNext()) {
				
				fileItem = iterator.next();
				
				fileName = URLEncoder.encode(fileItem.getName(), StandardCharsets.UTF_8.toString());
				
				String toPath = "public/" + accountId + "/" + fileName;
				storage().write(fileItem.openStream(), toPath);
				
				lines.append(Configuration.domain + "/public/" + accountId + "/" + fileName);
				lines.append("\n\n\n");
			}
			
		} catch (Exception e) {
			
			throw new RuntimeException(e + ":" + fileItem);
		}
		
		return lines.toString().trim();
	}	
	
}
