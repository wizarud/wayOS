package com.wayos.connector.http;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.wayos.Application;
import com.wayos.Configuration;
import com.wayos.PathStorage;
import com.wayos.MessageObject;
import com.wayos.Session;
import com.wayos.connector.RequestObject;
import com.wayos.util.URItoContextResolver;

import x.org.json.JSONObject;

public class HttpRequestObject extends RequestObject {
	
    protected final HttpServletRequest request;
    
    protected final HttpServletResponse response;
	    	
    protected final MessageObject messageObject;
    
	protected String signature;
	
	protected String message;
	
	protected String sessionId;	
	
	protected String contextName;
	
	public HttpRequestObject(HttpServletRequest request, HttpServletResponse response) {
		
		this.request = request;
		this.response = response;
						        
		messageObject = MessageObject.build();
		
    	setSignature(request.getHeader("Brainy-Signature"));
		
        message = request.getParameter("message");
		
        sessionId = request.getParameter("sessionId");
        
        /*
        System.err.println("Incomin Request");
        System.err.println("message:" + message);
        System.err.println("sessionId:" + sessionId);
        System.err.println();
        */
        
		try {
			
			URItoContextResolver uriToContextResolver = new URItoContextResolver(request);
			contextName = uriToContextResolver.contextName();
			
			/**
			 * For URI <accountId/botId/sessionId>
			 * Ex. upload file by XHR
			 */
			if (sessionId==null && uriToContextResolver.sessionId!=null) {
				
				sessionId = uriToContextResolver.sessionId;				
			}
			
		} catch (Exception e) {
			
			contextName = null;
		}
		
		messageObject.attr("channel", "http");
    }
	
	public HttpServletRequest httpServletRequest() {
		
		return request;
	}
	
	public HttpServletResponse httpServletResponse() {
		
		return response;
	}
	
	protected PathStorage storage() {
		
		//return (DirectoryStorage) request.getServletContext().getAttribute(DirectoryStorage.class.getName());		
		
		return Application.instance().get(PathStorage.class);
	}
	
	public void setSignature(String signature) {
		
		this.signature = signature;
	}

	@Override
	public MessageObject messageObject() {
		
        if (message==null) {
        	
            throw new IllegalArgumentException("Missing message parameter");
        }
        
        messageObject.setText(message);
        
		return messageObject;
	}

	@Override
	public String sessionId() {
		
        if (sessionId==null) {
        	
            throw new IllegalArgumentException("Missing sessionId parameter");
        }
        
		return sessionId;
	}

	@Override
	public String contextName() {
		
		return contextName;
	}
	
	@Override
	public void prepare(Session session) {
		
		if (isAdmin(session)) {
			
			messageObject.attr("selfSign", Configuration.brainySecret);
			
		}
		
		if (signature!=null) {
			
			messageObject.attr("signature", signature);
			
		}
		
        session.vars("#s_referer",  request.getHeader("referer"));
		
	}
	
	private boolean isAdmin(Session session) {
		
		String sessionId = session.vars("#sessionId");
		
		PathStorage storage = Application.instance().get(PathStorage.class);
		
		Configuration configuration = new Configuration(session.context().name());
		
		JSONObject configObject = storage.readAsJSONObject(configuration.adminIdPath());
		
		if (configObject!=null && configObject.getString("sessionId").equals(sessionId)) {
			
			return true;
		}
		
		return false;
	}

}
