package com.wayos.util;

import javax.servlet.http.HttpServletRequest;

public class URItoContextResolver {
	
	public final String accountId;
	public final String botId;
	public final String sessionId;
	
	public URItoContextResolver(HttpServletRequest request) {
		this(request.getRequestURI(), request.getContextPath().isEmpty());
	}
			
	public URItoContextResolver(String uri, boolean isRoot) {
		
		int forSessionIdPath = 4;
		int forContextNamePath = 3;
				
		if (!isRoot) {
			forSessionIdPath += 1;
			forContextNamePath += 1;
		}
		
        String [] uris = uri.split("/");
        
        if (uris.length>forSessionIdPath) {
        	
            accountId = uris[forSessionIdPath-2];
            botId = uris[forSessionIdPath-1];
            sessionId = uris[forSessionIdPath];
        	
        } else if (uris.length>forContextNamePath) {
        	
            accountId = uris[forContextNamePath-1];
            botId = uris[forContextNamePath];
            sessionId = null;
            
        } else {
        	
            throw new IllegalArgumentException("Invalid URI [../<accountId>/<botId>]");
        }					
	}
	
	public String contextName() {
		
		return accountId + "/" + botId;
	}
	
	public String sessionId() {
		
		return sessionId;
	}
	
	public static void main(String[]args) {
		
		URItoContextResolver uri = new URItoContextResolver("/<<anotherContextNaame>>/webhooks/<accountId>/<botId>/<sessionId>", true);
		
		System.out.println(uri.contextName());
		System.out.println(uri.sessionId());
	}
}
