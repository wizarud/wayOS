package com.wayos.util;

import com.wayos.Configuration;

public class URItoContextResolver {
	
	public final String accountId;
	public final String botId;
	public final String sessionId;
	
	public static boolean hasContextRoot() {
		
		return Configuration.contextRoot != null;
	}
	
	public URItoContextResolver(String uri) {
		
		int forSessionIdPath = 4;
		int forContextNamePath = 3;
				
		if (hasContextRoot()) {
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
		
		URItoContextResolver uri = new URItoContextResolver("/wayOSTomcat/webhooks/<accountId>/<botId>/<sessionId>");
		
		System.out.println(uri.contextName());
		System.out.println(uri.sessionId());
	}
}
