package com.wayos.util;

public class URItoContextResolver {
	
	public final String accountId;
	public final String botId;
	public final String sessionId;
	
	public URItoContextResolver(String uri) {
        String [] uris = uri.split("/");
        if (uris.length>4) {
        	
            accountId = uris[2];
            botId = uris[3];
            sessionId = uris[4];
        	
        } else if (uris.length>3) {
        	
            accountId = uris[2];
            botId = uris[3];
            sessionId = null;
            
        } else {
        	
            throw new IllegalArgumentException("Invalid URI [../<accountId>/<botId>]");
        }					
	}
	
	public String contextName() {
		return accountId + "/" + botId;
	}
}
