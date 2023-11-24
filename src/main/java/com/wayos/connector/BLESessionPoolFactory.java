package com.wayos.connector;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

import com.wayos.Application;
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
import com.wayos.context.PathStorageContext;
import com.wayos.pusher.PusherUtil;
import com.wayos.pusher.WebPusher;
import com.wayos.util.ConsoleUtil;

/**
 * 
 * Action Variable support for the following prefix if its' value is changed
 * 
 * #b_<name> boardcast to all session
 * #l_<name> log as report and notify to adminSessionId
 * #p_<contextName> fire parsed message
 * #m_ fire message as keywords to current context web client and parse later, only support same context and web
 * 
 * @author Wisarut Srisawet
 *
 */
public class BLESessionPoolFactory {
	
	private final PathStorage storage;
	
	private final ConsoleUtil consoleUtil;
	
	private final PusherUtil pusherUtil;
	
	public BLESessionPoolFactory(PathStorage storage, ConsoleUtil consoleUtil, PusherUtil pusherUtil) {
		
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
					
					String adminAccountId, adminBotId, adminChannel, adminSessionId;
					
					adminChannel = null;
					
					adminSessionId = null;
					
					//Check that called from other context or not?
					String callerContextName = session.vars("#caller.context.name");
					
					//Use this accountId, botId as adminAccountId and adminBotId
					if (callerContextName.isEmpty()) {
						
						adminAccountId = accountId;
						
						adminBotId = botId;
						
						adminConfigObject = storage.readAsJSONObject(configuration.adminIdPath());
						
					} else {
						
						tokens = callerContextName.split("/");
						
						adminAccountId = tokens[0];
						
						adminBotId = tokens[1];
						
						configuration = new Configuration(callerContextName);
						
						adminConfigObject = storage.readAsJSONObject(configuration.adminIdPath());
						
					}
												
					if (adminConfigObject!=null) {
						
						adminChannel = adminConfigObject.getString("channel");
						
						adminSessionId = adminConfigObject.getString("sessionId");
						
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
							
							pusherUtil.push(adminAccountId, adminBotId, varChangedValue);
							
						}
						
						/**
						 * Log it for reporting
						 */
						if (varChangedName.startsWith("#l_")) {
							
							consoleUtil.appendVars(null, accountId, botId, channel, sessionId, varChangedValue, "|");
							
							/**
							 * Notify to registered admin channel / sessionId to notify if this session is not from admin
							 */
							
							if (adminSessionId!=null && !adminSessionId.equals(sessionId)) {
								
								pusherUtil.push(adminAccountId, adminBotId, adminChannel, adminSessionId, varChangedValue);
								
							}
														
						}
						
						/**
						 * Fire message as a keyword to the current context and parse later
						 */
						if (varChangedName.startsWith("#m_")) {
							
							targetSessionId = varChangedName.substring("#m_".length());
							
							WebPusher webPusher = (WebPusher) Application.instance().get("web");
							
					    	JSONObject data = new JSONObject();
					    	
					    	data.put("fromAccountId", adminAccountId);
					    	data.put("fromBotId", adminBotId);
					    	data.put("fromSessionId", sessionId);
					    	data.put("message", varChangedValue);
							
							webPusher.push(adminAccountId + "/" + adminBotId, targetSessionId, data);
							
						}
						
						/**
						 * TODO: Not test yet!!!
						 * Fire parsed message to the target contextName
						 */
						if (varChangedName.startsWith("#p_")) {
							
							targetContextName = varChangedName.substring("#p_".length());
							
							tokens = targetContextName.split("/");
														
							if (tokens.length==1 && tokens[0].equals(".")) {
								
								/**
								 * Send varChangedValue to this context to parse and notify to adminChannel and adminSessionId
								 * 
								 * #p_.
								 */
								if (adminConfigObject!=null) {
									
									pusherUtil.parse(adminAccountId, adminBotId, adminChannel, adminSessionId, varChangedValue);
									
								}			
								
							} else if (tokens.length==2) {
								
								/**
								 * Send varChangedValue to target context to parse and notify to adminChannel and adminSessionId
								 * 
								 * #p_<accountId/botId>
								 */
								
								if (adminConfigObject!=null) {
									
									targetAccountId = tokens[0];
									
									targetBotId = tokens[1];
									
									pusherUtil.parse(targetAccountId, targetBotId, adminChannel, adminSessionId, varChangedValue);
									
								}
								
							} else if (tokens.length==3 && tokens[0].equals(".")) {
									
								/**
								 * Send varChangedValue to this context to parse and notify to targetChannel and targetSessionId
								 * 
								 * #p_<./channel/sessionId>
								 */
								targetChannel = tokens[1];
								
								targetSessionId = tokens[2];
									
								pusherUtil.parse(accountId, botId, targetChannel, targetSessionId, varChangedValue);
									
							} else if (tokens.length==4) {
								
								/**
								 * Send varChangedValue to target context to parse and notify to targetChannel and targetSessionId
								 * 
								 * #p_<accountId/botId/channel/sessionId>
								 */
								targetAccountId = tokens[0];
								
								targetBotId = tokens[1];
								
								targetChannel = tokens[2];
								
								targetSessionId = tokens[3];
								
								pusherUtil.parse(targetAccountId, targetBotId, targetChannel, targetSessionId, varChangedValue);
								
							}
							
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
