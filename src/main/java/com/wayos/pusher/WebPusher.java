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

import com.wayos.PathStorage;
import com.wayos.connector.ResponseObject;

import x.org.json.JSONObject;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.Session;

@ServerEndpoint(value = "/websocket/{accountId}/{botId}/{sessionId}")
public class WebPusher extends Pusher {
	
	/**
	 * For boardcast to <accountId>/<botId> => sessionId
	 */
	private static Map<String, Set<String>> contextToSessionIdSetMap = new HashMap<>();
	
	/**
	 * For boardcast to sessionId => <accountId>/<botId>
	 */
	private static Map<String, Set<String>> sessionToContextSetMap = new HashMap<>();
	
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
	    
	    /**
	     * TODO: overload method for send with fromSessionId..
	     *
	     */
	    
	    send(accountId, botId, sessionId, message);
	    
	}
	
	@OnOpen
    public void start(Session session, @PathParam("accountId") String accountId, @PathParam("botId") String botId, @PathParam("sessionId") String sessionId) {
						
		System.out.println("Incoming connection from.." + accountId + "/" + botId + "/" + sessionId);
		
		/**
		 * Group sessionId by contextName
		 */
        Set<String> sessionIdSet = contextToSessionIdSetMap.get(accountId + "/" + botId);
        
        if (sessionIdSet==null) {
        	
        	sessionIdSet = new HashSet<>();
        	
        }
        
        sessionIdSet.add(sessionId);
        
        contextToSessionIdSetMap.put(accountId + "/" + botId, sessionIdSet);
        
        /**
         * Group contextName by sessionId
         */
        Set<String> contextNameSet = sessionToContextSetMap.get(sessionId);
        
        if (contextNameSet==null) {
        	
        	contextNameSet = new HashSet<>();
        	
        }
        
        contextNameSet.add(accountId + "/" + botId);
        
        sessionToContextSetMap.put(sessionId, contextNameSet);
        
        /**
         * Use full reference to session instance for pushing
         */
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
    	
        Set<String> connectionSet = contextToSessionIdSetMap.get(accountId + "/" + botId);
    	
        if (connectionSet!=null) {
        	
        	connectionSet.remove(sessionId);
        	
        }
        
        if (connectionSet!=null && connectionSet.isEmpty()) {
        	
        	contextToSessionIdSetMap.remove(accountId + "/" + botId);
        	
        }
    	
    	connectionListMap.remove(accountId + "/" + botId + "/" + sessionId);
    	
    }
    
    /**
     * Send to targets that have same AccountId, botId
     * @param fromAccountId
     * @param fromBotId
     * @param fromSessionId
     * @param accountId
     * @param botId
     * @param message
     */
    public static void boardcast(String fromAccountId, String fromBotId, String fromSessionId, String accountId, String botId, String message) {
    	
        Set<String> connectionSet = contextToSessionIdSetMap.get(accountId + "/" + botId);
                
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
    
    /**
     * Send to targets that have same sessionId
     * @param fromAccountId
     * @param fromBotId
     * @param fromSessionId
     * @param sessionId
     * @param message
     */
    public static void boardcast(String fromAccountId, String fromBotId, String sessionId, String message) {
    
    	String fromContextName = fromAccountId + "/" + fromBotId;
    	
        Set<String> connectionSet = sessionToContextSetMap.get(sessionId);
    
        String [] tokens;
        String accountId, botId;
        for (String contextName : connectionSet) {
        	
        	if (fromContextName.equals(contextName)) {
        		//Skip own message from same context
        		continue;
        	}
        	
        	tokens = contextName.split("/");
        	accountId = tokens[0];
        	botId = tokens[1];
        	
        	send(accountId, botId, sessionId, message);

        }
   	
    }
    
    public static void send(String accountId, String botId, String sessionId, String message) {
    	
        List<Session> sessionList = null;
        
        try {
        	
        	System.out.println("Sending " + message + " to " + accountId + "/" + botId + "/" + sessionId);
        	
        	//ResponseObject responseObject = new ResponseObject(message);
        	        	
        	sessionList = connectionListMap.get(accountId + "/" + botId + "/" + sessionId);
        	
        	if (sessionList!=null) {
        		
            	for (Session session:sessionList) {
            		
                    synchronized (session) {
                    	
                    	//session.getBasicRemote().sendText(responseObject.toJSONString());
                    	                                    	
                    	//session.getBasicRemote().sendText(message);
                    	
                    	session.getBasicRemote().sendText(new ResponseObject(message).toJSONString());
                    }
            		
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
