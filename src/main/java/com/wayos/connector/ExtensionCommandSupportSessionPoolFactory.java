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
					
					String toAccountId, toBotId, toChannel, toSessionId;
					
					toChannel = null;
					
					toSessionId = null;
					
					//Check that called from other context or not?
					String callerContextName = session.vars("#caller.context.name");
					
					//Use this accountId, botId as adminAccountId and adminBotId
					if (callerContextName.isEmpty()) {
						
						toAccountId = accountId;
						
						toBotId = botId;
						
						adminConfigObject = storage.readAsJSONObject(configuration.adminIdPath());
						
					} else {
						
						tokens = callerContextName.split("/");
						
						toAccountId = tokens[0];
						
						toBotId = tokens[1];
						
						configuration = new Configuration(callerContextName);
						
						adminConfigObject = storage.readAsJSONObject(configuration.adminIdPath());
						
					}
												
					if (adminConfigObject!=null) {
						
						toChannel = adminConfigObject.getString("channel");
						
						toSessionId = adminConfigObject.getString("sessionId");
						
					}
					
					Set<String> varChangedNameSet = session.getVariableChangedNameSet();
					
					String varChangedValue;
					
					String targetContextName;
					
					String targetAccountId, targetBotId;
					
					String targetChannel, targetSessionId;
					
					for (String varChangedName:varChangedNameSet) {
						
						varChangedValue = session.vars(varChangedName);
						
						if (varChangedValue.trim().isEmpty()) continue;
						
						/**
						 * Broadcast to all sessions that involve this contextName
						 */
						if (varChangedName.startsWith("#b_")) {
							
							pusherUtil.push(toAccountId, toBotId, varChangedValue);
							
						}
						
						/**
						 * Log it for reporting
						 */
						if (varChangedName.startsWith("#l_")) {
							
							consoleUtil.appendLogVars(null, accountId, botId, channel, sessionId, varChangedValue, "|");
							
							/**
							 * Notify to registered admin channel / sessionId to notify if this session is not from admin
							 */							
							if (toSessionId!=null && !toSessionId.equals(sessionId)) {
								
								//System.out.println("Try to push message to admin " + toChannel + "/" + toSessionId);
								
								pusherUtil.push(toAccountId, toBotId, toChannel, toSessionId, varChangedValue);
								
							}
														
						}
						
						/**
						 * Only Web Support!!!
						 * Fire message as a keyword to the current session of context and parse later
						 */
						if (varChangedName.startsWith("#m_")) {
														
							//targetSessionId = varChangedName.substring("#m_".length());
							
							targetSessionId = sessionId;//Support Fire from API sessionId=<sessionId>&message=<key>
							
							WebPusher webPusher = (WebPusher) Application.instance().get("web");
							
					    	JSONObject data = new JSONObject();
					    	
					    	data.put("fromAccountId", toAccountId);
					    	data.put("fromBotId", toBotId);
					    	data.put("fromSessionId", sessionId);
					    	data.put("message", varChangedValue);
							
							webPusher.push(toAccountId + "/" + toBotId, targetSessionId, data);
							
						}
						
						/**
						 * 
						 * TODO: #f_xxx
						 * Fun Var!
						 * Push forward signal to make x parse that message.
						 * Like Redirect in HttpResponse
						 * 
						 */
						if (varChangedName.startsWith("#f_")) {
							
					    	WebPusher.send(accountId, botId, sessionId, varChangedValue, "forward");
							
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
