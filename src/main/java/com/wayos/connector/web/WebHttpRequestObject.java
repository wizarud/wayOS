package com.wayos.connector.web;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

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
        	
        	message += "\n\n\n" + tryUpload(request);
        }
        
        messageObject.attr("channel", "web");
    }
		
	private String tryUpload(HttpServletRequest request) {
		
		StringBuilder lines = new StringBuilder();
		
		String accountId = (String) request.getSession().getAttribute("accountId");		
		
		boolean isOwner = accountId!=null && contextName.startsWith(accountId);
		
		String [] tokens = contextName.split("/");
		
		String botId = tokens[1];
		
		try {
			
			ServletFileUpload upload = new ServletFileUpload();
			FileItemIterator iterator = upload.getItemIterator(request);
										
			FileItemStream fileItem;
			
			while (iterator.hasNext()) {
				
				fileItem = iterator.next();
				
				if (isOwner) {
					
					//Update Context from context file
					if (fileItem.getName().equals(botId + ".context")) {
						
						continue;
					}
					
					//Update Context from TSV
					if (fileItem.getName().endsWith(".tsv")) {
						
						lines.append("importtsv");
						lines.append(System.lineSeparator());
						lines.append(new BufferedReader(new InputStreamReader(fileItem.openStream(), StandardCharsets.UTF_8))
				        .lines()
				        .collect(Collectors.joining("\n")));
						lines.append("\n");
						
						continue;
					}
					
				}
				
				String toPath = "public/" + accountId + "/" + URLEncoder.encode(fileItem.getName(), "UTF-8");
				storage().write(fileItem.openStream(), toPath);
				
				lines.append(Configuration.domain + "/public/" + accountId + "/" + URLEncoder.encode(fileItem.getName(), "UTF-8"));
				lines.append("\n\n\n");
			}
			
		} catch (Exception e) {
			
			throw new RuntimeException(e);
		}
		
		return lines.toString().trim();
	}	
	
}
