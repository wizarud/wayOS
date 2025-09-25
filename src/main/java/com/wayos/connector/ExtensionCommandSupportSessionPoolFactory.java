package com.wayos.connector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;

import com.wayos.Application;
import com.wayos.Configuration;
import com.wayos.Context;
import com.wayos.ContextListener;
import com.wayos.PathStorage;
import com.wayos.NodeEvent;
import com.wayos.Session;
import com.wayos.SessionListener;
import com.wayos.command.admin.AdminCommandNode;
import com.wayos.command.wakeup.ExtensionSupportWakeupCommandNode;
import com.wayos.connector.SessionPool.ContextFactory;
import com.wayos.context.PathStorageContext;
import com.wayos.pusher.PusherUtil;
import com.wayos.pusher.WebPusher;
import com.wayos.util.ConsoleUtil;

import x.org.json.JSONObject;

public class ExtensionCommandSupportSessionPoolFactory {
	
	private final ServletContext servletContext;
	
	private final PathStorage storage;
	
	private final ConsoleUtil consoleUtil;
	
	private final PusherUtil pusherUtil;
	
	public ExtensionCommandSupportSessionPoolFactory(ServletContext servletContext, PathStorage storage, ConsoleUtil consoleUtil, PusherUtil pusherUtil) {
		
		this.servletContext = servletContext;
		
		this.storage = storage;
		
		this.consoleUtil = consoleUtil;
		
		this.pusherUtil = pusherUtil;
		
	}
	
	public SessionPool create() {
	
		SessionPool sessionPool = new SessionPool();
		
    	/**
    	 * Basic Configuration for Google AppEngine
    	 */
		sessionPool.register(new ContextFactory() {

			@Override
			public Context createContext(String contextName) {
				
				return new PathStorageContext(storage, contextName);
			}
    		
    	});    	
    	
		sessionPool.register(new ContextListener() {

			@Override
			public void callback(NodeEvent nodeEvent) {
				
		        if (nodeEvent.event==NodeEvent.Event.Authentication) {
		        	
		            throw new AdminCommandNode.AuthenticationException("Invalid Signature for Brainy Admin Command:" + nodeEvent.messageObject);
		        	
		        }

		        if (nodeEvent.event==NodeEvent.Event.ContextSaved) {

		            String contextName = nodeEvent.messageObject.toString();
		            if (contextName.endsWith(".backup")) return;
		            
		            System.out.println(contextName + " Updated!!!!");
		            
	            	/**
	            	 * TODO: Notify Logic Designer for Advance Debugging
	            	 */            	
					WebPusher webPusher = (WebPusher) Application.instance().get("web");
					
			    	String [] tokens = contextName.split("/");
			    	String toAccountId = tokens[0];
			    	String toBotId = tokens[1];
			    	
			    	//String fromSessionId = session.vars("#sessionId");
			    	String targetSessionId = "logic-designer";
			    	
			    	JSONObject data = new JSONObject();
			    	
			    	data.put("type", "update");
			    	data.put("fromAccountId", toAccountId);
			    	data.put("fromBotId", toBotId);
			    	//data.put("fromSessionId", fromSessionId);
			    	//data.put("nodeId", nodeEvent.node.id());
			    	data.put("message", nodeEvent.messageObject.toString());
			    					
					webPusher.push(contextName, targetSessionId, data);
		            

		        }
		        
		   }
    		
    	});
		
		sessionPool.register(new SessionPool.SessionPoolListener() {

			@Override
			public void onNewSession(RequestObject requestObject, Session session) {
			
				/**
				 * To enable Session Persistent Variables
				 */
				
				String contextName = session.context().name();
				
				Configuration configuration = new Configuration(contextName);
				
		        String channel = (String) requestObject.messageObject().attr("channel");
		        
		        String sessionId = requestObject.sessionId();
		        
		        String path = configuration.vars(channel, sessionId);
								
		        JSONObject varsObject = storage.readAsJSONObject(path);

	        	/**
	        	 * Update Variables, Uses variableMap to avoid variablesChangedEvent.
	        	 */

		        if (varsObject!=null) {
		        	
		        	try {
		        											    
		        		//removeUnusedVariables(session, prop);
		        		
		        		Map<String, Object> prop = varsObject.toMap();
		        		
						for (Map.Entry<String, Object> entry:prop.entrySet()) {
							
							session.vars().put(entry.getKey(), entry.getValue().toString());
						}
						
						//Update Session Persistent
						storage.write((new JSONObject(prop).toString()), path);
						
						
		        	} catch (Exception e) {
		        		
						throw new RuntimeException(e);
					}

		        }
				
		        /**
		         * Create Build In Variables
		         * Uses variableMap to avoid variablesChangedEvent
		         */
		        		        
		        session.vars("#channel", channel);
		        session.vars("#sessionId", sessionId);
		        
		        /*
		        if (session.vars("#channel").isEmpty()) {
		        	
		        	throw new RuntimeException("Unknown channel");
		        }
		        */
		        
        	    /**
        	     * Check that this session is in Edit Mode, So switch context to adminContext
        	     */
        	    String adminContextName = session.vars("wayobot.context.admin.name");
        	    
        	    if (adminContextName!=null && !adminContextName.isEmpty()) {
        	    	
            		session.context(sessionPool.getContext(adminContextName));
        	    }
                
			}

			@Override
			public void onRemoveSession(Session session) {
				
				//notifyUtil.silentNotification(session);			
			}

			@Override
			public void onTime(Session session) {
				
				//notifyUtil.silentNotification(session);
			}

    	});
		
		sessionPool.register(new SessionListener() {

			@Override
			public void callback(NodeEvent nodeEvent) {
				
			}

			@Override
			public void onVariablesChanged(Session session) {
				
				//System.out.println("On Vars Change");
				
				String channel = session.vars("#channel");
				
				String sessionId = session.vars("#sessionId");
				
				String contextName = session.context().name();
								
				String [] tokens = contextName.split("/");
				
				String accountId = tokens[0];
				
				String botId = tokens[1];
				
				Configuration configuration = new Configuration(contextName);
				
				String path = configuration.vars(channel, sessionId);
								
				try {
					
			        /**
			         * Merged to variable file
			         */
					
					//Load session variables from json file
					Map<String, Object> prop;

			        JSONObject varsObject = storage.readAsJSONObject(path);
			        
			        if (varsObject!=null) {
			        	
			        	prop = varsObject.toMap();
			        	
			        } else {
			        	
			        	prop = new HashMap<>();
			        	
			        }
					
					//Remove Removed Variables					
		        	Set<String> variablesSet = session.vars().keySet();
		        	Set<String> propVarsSet = new HashSet<>(prop.keySet());
		        	for (String varName:propVarsSet) {
		        		if (!variablesSet.contains(varName)) {
		        			prop.remove(varName);
		        		}
		        	}
					
		        	//Update Variable
					Map<String, String> variableMap = session.vars();
					for (Map.Entry<String, String> entry:variableMap.entrySet()) {
						
						prop.put(entry.getKey(), entry.getValue());
						
					}
					
					//Save current state					
					//System.out.println("Save " + (new JSONObject(prop).toString()) + " to.." + path);					
					storage.write((new JSONObject(prop).toString()), path);
					
					/**
					 * Process Action Variables!
					 */					
					
					JSONObject adminConfigObject;
					
					String adminChannel, adminSessionId;
										
					adminConfigObject = storage.readAsJSONObject(configuration.adminIdPath());
																	
					if (adminConfigObject!=null) {
						
						adminChannel = adminConfigObject.getString("channel");
						
						adminSessionId = adminConfigObject.getString("sessionId");
						
					} else {
						
						adminChannel = null;
						
						adminSessionId = null;
						
					}
					
					Set<String> varChangedNameSet = session.getVariableChangedNameSet();
					
					String varChangedValue;
					
					for (String varChangedName:varChangedNameSet) {
						
						varChangedValue = session.vars(varChangedName);
						
						if (varChangedValue.trim().isEmpty()) continue;
						
						/**
						 * Log and Push to admin session
						 * Also Support LINE channel too
						 */
						if (varChangedName.startsWith("#l_")) {
							
							consoleUtil.appendLogVars(null, accountId, botId, channel, sessionId, varChangedValue, "|");
							
							if (adminSessionId!=null && !adminSessionId.equals(sessionId)) {
								
								pusherUtil.push(accountId, botId, adminChannel, adminSessionId, varChangedValue);
								
							}
														
						}
												
						
						/**
						 * Push notification to this session
						 */
						if (varChangedName.startsWith("#n_")) {
																					
					    	WebPusher.send(accountId, botId, sessionId, varChangedValue, null);
					    	
						}
						
						/**
						 * Push message to this session and forward at x.jsp client
						 */
						if (varChangedName.startsWith("#k_")) {
							
					    	WebPusher.send(accountId, botId, sessionId, varChangedValue, "forward");
							
						}
						
						/**
						 * Push Message to admin session
						 */							
						if (varChangedName.startsWith("#a_")) {
							
							if (adminSessionId!=null && !adminSessionId.equals(sessionId)) {
								
						    	WebPusher.send(accountId, botId, adminSessionId, varChangedValue, "forward");
							}
														
						}
				    											
					}
										
				} catch (Exception e) {
					
					e.printStackTrace();
					
					//throw new RuntimeException(e);
				}
				
			}

			@Override
			public void onContextChanged(Session session, Context oldContext, Context newContext) {
				
				ExtensionSupportWakeupCommandNode wakeupCommandNode = new ExtensionSupportWakeupCommandNode(session);
				
				/**
				 * TODO: Add from context attribute
				 */
				List<ExtensionSupportWakeupCommandNode.NewSessionListener> newSessionListenerList = 
						(List<ExtensionSupportWakeupCommandNode.NewSessionListener>) servletContext.getAttribute("ExtensionSupportWakeupCommandNode.NewSessionListener");
				
				if (newSessionListenerList!=null) {
					
					wakeupCommandNode.addNewSessionListener(newSessionListenerList);
					
				}
				
				wakeupCommandNode.execute(null);

			}

		});		
		
		return sessionPool;
	}
	
	
}
