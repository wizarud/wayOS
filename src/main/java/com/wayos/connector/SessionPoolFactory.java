package com.wayos.connector;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

import com.wayos.Configuration;
import com.wayos.Context;
import com.wayos.ContextListener;
import com.wayos.PathStorage;
import com.wayos.NodeEvent;
import com.wayos.Session;
import com.wayos.SessionListener;
import com.wayos.command.CommandNode;
import com.wayos.command.admin.AdminCommandNode;
import com.wayos.command.wakeup.WAYOSWakeupCommandNode;
import com.wayos.connector.SessionPool.ContextFactory;
import com.wayos.context.DirectoryStorageContext;
import com.wayos.pusher.PusherUtil;
import com.wayos.util.Application;
import com.wayos.util.ConsoleUtil;

public class SessionPoolFactory {
	
	private final PathStorage storage;
	
	private final ConsoleUtil consoleUtil;
	
	private final PusherUtil pusherUtil;
	
	public SessionPoolFactory(PathStorage storage, ConsoleUtil consoleUtil, PusherUtil pusherUtil) {
		
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
				
				return new DirectoryStorageContext(storage, contextName);
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

		        }
		        
		   }
    		
    	});
		
		sessionPool.register(new SessionPool.SessionPoolListener() {

			@Override
			public void onNewSession(RequestObject requestObject, Session session) {
			
				String contextName = session.context().name();
				
				Configuration configuration = new Configuration(contextName);
				
		        String channel = (String) requestObject.messageObject().attributes.get("channel");
		        
		        String sessionId = requestObject.sessionId();
		        
		        String path = configuration.vars(channel, sessionId);
								
		        JSONObject varsObject = storage.readAsJSONObject(path);

	        	/**
	        	 * Update Variables, Uses variableMap to avoid variablesChangedEvent.
	        	 */

		        if (varsObject!=null) {
		        	
		        	try {
		        		
		        		Map<String, Object> prop = varsObject.toMap();
									    
		        		//removeUnusedVariables(session, prop);
		        		
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
				
				String channel = session.vars("#channel");
				
				String sessionId = session.vars("#sessionId");
				
				String contextName = session.context().name();
				
				String [] tokens = contextName.split("/");
				
				String accountId = tokens[0];
				
				String botId = tokens[1];
				
				Configuration configuration = new Configuration(contextName);
				
				String path = configuration.vars(channel, sessionId);
				
				/**
				 * Load session variables from json file
				 */
				Map<String, Object> prop;
								
		        JSONObject varsObject = storage.readAsJSONObject(path);
		        
		        if (varsObject!=null) {
		        	
		        	prop = varsObject.toMap();
		        	
		        } else {
		        	
		        	prop = new HashMap<>();
		        	
		        }
				
		        /**
		         * Merged to variable file
		         */
				try {
					
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
					storage.write((new JSONObject(prop).toString()), path);
					
					PathStorage storage = Application.instance().get(PathStorage.class);
					
					JSONObject adminConfigObject = storage.readAsJSONObject(configuration.adminIdPath());					
					
					/**
					 * Process Action Variables!
					 */
					Set<String> varChangedNameSet = session.getVariableChangedNameSet();
					
					for (String varChangedName:varChangedNameSet) {
						
						/**
						 * Log if variable changed name is configured
						 */
						if (varChangedName.startsWith("#l_")) {
							
							String varChangedValue = session.vars(varChangedName);
							
							consoleUtil.appendVars(null, accountId, botId, channel, sessionId, varChangedValue, "|");
							
							/**
							 * Notify to registered admin channel / sessionId to notify
							 */
							
							if (adminConfigObject!=null) {
								
								/**
								 * TODO: Edit varChangedValue message again
								 */
								pusherUtil.push(accountId, botId, adminConfigObject.getString("channel"), adminConfigObject.getString("sessionId"), varChangedValue);
								
							}
														
						}
						
						/**
						 * TODO: Broadcast to all sessions that involve this contextName
						 */
						if (varChangedName.startsWith("#b_")) {
							
						}
					}
										
				} catch (Exception e) {
					
					throw new RuntimeException(e);
				}
				
			}

			@Override
			public void onContextChanged(Session session, Context oldContext, Context newContext) {
				
				createWakeupCommandNode(session).execute(null);

			}

		});		
		
		return sessionPool;
	}
	
	/**
	 * Register Wakeup Commands
	 */
	private CommandNode createWakeupCommandNode(Session session) {
		
		CommandNode wakeupCommandNode;
		
		/**
		 * Try with wayobot.context.wakeup.impl property
		 */
		if (session.context().prop("wayobot.context.wakeup.impl")!=null) {
			
			try {
				
				Class commandNodeClass = Class.forName(session.context().prop("wayobot.context.wakeup.impl"));
				
				Constructor commandNodeConstructor = commandNodeClass.getDeclaredConstructor(Session.class);
				
				commandNodeConstructor.setAccessible(true);
				
				wakeupCommandNode = (CommandNode) commandNodeConstructor.newInstance(session);
				
			} catch (Exception e) {
				
				throw new RuntimeException(e);
				
			}
			
		} 
		
		/**
		 * Default WakeupCommandNode
		 */
		else {
			
		    wakeupCommandNode = new WAYOSWakeupCommandNode(session);                	
		    
		}
		
		return wakeupCommandNode;
	}	

}
