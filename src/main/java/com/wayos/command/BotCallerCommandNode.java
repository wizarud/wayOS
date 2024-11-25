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
		
		String [] tokens = params.split(" ", 2);
		
		if (tokens.length!=2) {
			System.out.println("BotCallerCommand (Tokens.length!=2):" + messageObject);
			return "";
		}
		
		String sessionId = session.vars().get("#sessionId");
		String botName = tokens[0];
		String sendMessage = tokens[1].trim();
		String toContextName = session.context().name().split("/")[0] + "/" + botName;
		
		/*
		System.out.println("BotCallerCommandNode");
		System.out.println("sessionId: " + sessionId);
		System.out.println("toContextName: " + toContextName);
		System.out.println("sendMessage: " + sendMessage);
		*/
		
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
