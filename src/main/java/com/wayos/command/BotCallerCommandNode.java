package com.wayos.command;

import com.wayos.Application;
import com.wayos.MessageObject;
import com.wayos.Session;
import com.wayos.Hook.Match;
import com.wayos.connector.RequestObject;
import com.wayos.connector.SessionPool;

public class BotCallerCommandNode extends CommandNode {

	public BotCallerCommandNode(Session session, String[] hooks) {
		super(session, hooks);
	}
	
	public BotCallerCommandNode(Session session, String[] hooks, Match match) {
		super(session, hooks, match);
	}

	@Override
	public String execute(MessageObject messageObject) {
		
		String params = cleanHooksFrom(messageObject.toString());
		
		/*
		String [] tokens = params.split(" ", 2);
		
		if (tokens.length!=2) {
			System.out.println("BotCallerCommand (Tokens.length!=2):" + messageObject);
			return "";
		}
		*/
		
		/**
		 * TODO: To support other sessionId
		 * <botName> <sessionId> <message>
		 * Check tokens.length >= 3
		 * Or
		 * <botName> <message>
		 * tokens.length >= 2
		 */
		
		String [] tokens = params.split(" ", 3);
		
		String botName, sessionId, sendMessage;
		
		if (tokens.length >= 3 ) {
			
			botName = tokens[0];
			sessionId = tokens[1];
			sendMessage = tokens[2].trim();
			
		} else if (tokens.length == 2 ) {
			
			botName = tokens[0];
			sessionId = session.vars().get("#sessionId");
			sendMessage = tokens[1].trim();
			
		} else {
			
			System.out.println("BotCallerCommand (Tokens.length!=2):" + messageObject);
			return "";
			
		}
				
		String toContextName = session.context().name().split("/")[0] + "/" + botName;
		
		System.out.println("BotCallerCommandNode");
		System.out.println("sessionId: " + sessionId);
		System.out.println("toContextName: " + toContextName);
		System.out.println("sendMessage: " + sendMessage);
		
		RequestObject requestObject = RequestObject.create("http", sessionId, toContextName);
		
		SessionPool sessionPool = Application.instance().get(SessionPool.class);
		
		try {
			
	        Session toSession = sessionPool.get(requestObject);
			
			/**
			 * For BLESessionPoolFactory, assign caller's context name.
			 */
	        //requestObject.prepare(toSession);
	        toSession.vars("#caller.context.name", session.context().name());
	        
			String responseText = toSession.parse(MessageObject.build(sendMessage)).trim();
			
			/*
			if (responseText.startsWith("สรุปคะแนน")) {
				responseText = "balbalba";
			}
			*/
						
			//System.out.println("responseText: " + responseText);
			//System.out.println();
			
			return responseText;
			
		} catch (Exception e) {
			
			e.printStackTrace();
			return e.getMessage();
			
		}
		
	}

}
