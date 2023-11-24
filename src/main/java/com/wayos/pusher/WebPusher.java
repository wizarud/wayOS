package com.wayos.pusher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.websocket.OnOpen;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.json.JSONObject;

import com.wayos.PathStorage;
import com.wayos.connector.ResponseObject;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.Session;

@ServerEndpoint(value = "/websocket/{accountId}/{botId}/{sessionId}")
public class WebPusher extends Pusher {
	
	/**
	 * For boardcast to <accountId>/<botId>
	 */
	private static Map<String, Set<String>> connectionSetMap = new HashMap<>();
	
	/**
	 * For direct to target
	 */
	private static Map<String, List<Session>> connectionListMap = new HashMap<>();
	
	private String accountId;
	
	private String botId;
	
	private String sessionId;
	
	public WebPusher() {}
	
	public WebPusher(PathStorage storage) {
		
		super(storage);
		
	}
	
	@Override
	public void push(String contextName, String sessionId, String message) {
		
		String [] tokens = contextName.split("/");
		
		String accountId = tokens[0];
		
		String botId = tokens[1];
					    
	    send(accountId, botId, sessionId, message);
	    
	}
	
	/**
	 * For m_ var
	 * @param contextName
	 * @param sessionId
	 * @param data
	 */
	public void push(String contextName, String sessionId, JSONObject data) {
		
		String [] tokens = contextName.split("/");
		
		String accountId = tokens[0];
		
		String botId = tokens[1];
				
    	String fromAccountId = data.optString("fromAccountId");
    	String fromBotId = data.optString("fromBotId");
    	String fromSessionId = data.optString("fromSessionId");
    	String message = data.optString("message");
    	
	    if (sessionId.equals("*")) {
	    
	    	boardcast(fromAccountId, fromBotId, fromSessionId, accountId, botId, message);
	    	
	    	return;
	    }
	    
	    send(accountId, botId, sessionId, message);
	    
	}
	
	@OnOpen
    public void start(Session session, @PathParam("accountId") String accountId, @PathParam("botId") String botId, @PathParam("sessionId") String sessionId) {
		
      	/*
		Map<String, List<String>> paramsMap = session.getRequestParameterMap();
		
		String accountId = paramsMap.get("accountId").get(0);
		
		String botId = paramsMap.get("botId").get(0);
		
		String sessionId = paramsMap.get("sessionId").get(0);
		*/
		
		/*
		HttpSession httpSession = (HttpSession) config.getUserProperties().get("httpSession");
		
        ServletContext servletContext = httpSession.getServletContext();
        
		URItoContextResolver uriToContextResolver = new URItoContextResolver(session.getRequestURI().toString(), servletContext.getContextPath().isEmpty());
		
		String accountId = uriToContextResolver.accountId;
		
		String botId = uriToContextResolver.botId;
		
		String sessionId = uriToContextResolver.sessionId;
		*/
				
		System.out.println("Incoming connection from.." + accountId + "/" + botId + "/" + sessionId);
		
        Set<String> connectionSet = connectionSetMap.get(accountId + "/" + botId);
        
        if (connectionSet==null) {
        	
        	connectionSet = new HashSet<>();
        	
        }
        
        connectionSet.add(sessionId);
        
        connectionSetMap.put(accountId + "/" + botId, connectionSet);
        
        List<Session> connectionList = connectionListMap.get(accountId + "/" + botId + "/" + sessionId);
        
        if (connectionList==null) {
        	
        	connectionList = new ArrayList<>();
        	
        }
        
        connectionList.add(session);
        
        connectionListMap.put(accountId + "/" + botId + "/" + sessionId, connectionList);
        
        //For remove later if end of connection
        this.accountId = accountId;
        
        this.botId = botId;
        
        this.sessionId = sessionId;
    }


    @OnClose
    public void end() {
    	
        remove(this.accountId, this.botId, this.sessionId);
        
    }

    @OnMessage
    public void incoming(String message) {
    	
    	
    }	
    
    @OnError
    public void onError(Throwable t) throws Throwable {
    	
    	//t.printStackTrace();
        //remove(this.accountId, this.botId, this.sessionId);
    	
    }
    
    private static void remove(String accountId, String botId, String sessionId) {
    	
    	System.out.println("Remove connection:" + accountId + "/" + botId + "/" + sessionId);
    	
        Set<String> connectionSet = connectionSetMap.get(accountId + "/" + botId);
    	
        if (connectionSet!=null) {
        	
        	connectionSet.remove(sessionId);
        	
        }
        
        if (connectionSet!=null && connectionSet.isEmpty()) {
        	
        	connectionSetMap.remove(accountId + "/" + botId);
        	
        }
    	
    	connectionListMap.remove(accountId + "/" + botId + "/" + sessionId);
    	
    }
    
    public static void boardcast(String fromAccountId, String fromBotId, String fromSessionId, String accountId, String botId, String message) {
    	
        Set<String> connectionSet = connectionSetMap.get(accountId + "/" + botId);
                
        for (String sessionId : connectionSet) {
        	
        	if (fromAccountId.equals(accountId) &&
        			fromBotId.equals(botId) &&
        				fromSessionId.equals(sessionId)) {
        		//Skip own message from same context
        		continue;
        	}
        	
        	send(accountId, botId, sessionId, message);

        }        
    	    	
    }
    
    public static void send(String accountId, String botId, String sessionId, String message) {
    	
        List<Session> sessionList = null;
        
        try {
        	
        	System.out.println("Sending " + message + " to " + accountId + "/" + botId + "/" + sessionId);
        	
        	ResponseObject responseObject = new ResponseObject(message);
        	        	
        	sessionList = connectionListMap.get(accountId + "/" + botId + "/" + sessionId);
        	
        	for (Session session:sessionList) {
        		
                synchronized (session) {
                	
                	session.getBasicRemote().sendText(responseObject.toJSONString());
                	
                }
        		
        	}
        	
            
        } catch (Exception e) {
        	
            System.out.println("Failed to send message to client :" + e);
            
            remove(accountId, botId, sessionId);
            
            try {
            	
            	if (sessionList!=null) {
            		
                	for (Session session:sessionList) {
                		
                        session.close();
                        
                	}
            		
            	}
                
            } catch (Exception e1) {
                // Ignore
            }
            
        }    	
    }
    

}
