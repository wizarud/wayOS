package com.wayos.command.admin;

import com.wayos.Application;
import com.wayos.Context;
import com.wayos.MessageObject;
import com.wayos.Session;
import com.wayos.Hook.Match;
import com.wayos.command.CommandNode;
import com.wayos.connector.SessionPool;

@SuppressWarnings("serial")
public class ManualCommandNode extends CommandNode {

	public ManualCommandNode(Session session, String[] hooks, Match match) {
		super(session, hooks, match);
	}

	@Override
	public String execute(MessageObject messageObject) {
		
		/**
		 * Restore contextName from session var
		 */	
		String contextName = session.vars("wayobot.context.name");
		
		if (!contextName.isEmpty()) {
			
			try {
				
	    		SessionPool sessionPool = Application.instance().get(SessionPool.class);
				Context context = sessionPool.getContext(contextName);
				context.prop("MANUAL", "true");
				context.save();
				context.load();
				
			} catch (Exception e) {
				
				throw new RuntimeException(e);
			}
						
			return super.successMsg();			
		}
		
		return super.failMsg();	
	}

}
