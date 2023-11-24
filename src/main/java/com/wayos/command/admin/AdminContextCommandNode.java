package com.wayos.command.admin;

import com.wayos.MessageObject;
import com.wayos.Session;
import com.wayos.Application;
import com.wayos.Configuration;
import com.wayos.Hook.Match;
import com.wayos.command.CommandNode;
import com.wayos.connector.SessionPool;

@SuppressWarnings("serial")
public class AdminContextCommandNode extends CommandNode {
		
	public AdminContextCommandNode(Session session, String[] hooks, Match match) {
		super(session, hooks, match);
	}

	@Override
	public String execute(MessageObject messageObject) {
		
		/**
		 * Save current context to session var
		 */
		
		session.vars("wayobot.context.name", session.context().name());
		
		String adminContextName;
		
		if (session.context().prop("wayobot.context.admin.name")!=null) {
			
			adminContextName = session.context().prop("wayobot.context.admin.name");
			
		} else {
			
			adminContextName = Configuration.adminContextName;
			
		}
		
		if (adminContextName!=null) {
			
    		session.vars("wayobot.context.admin.name", adminContextName);
    		
    		session.fireVariablesChangedEvent();//Store Variables
    		
    		SessionPool sessionPool = Application.instance().get(SessionPool.class);
    		
    		session.context(sessionPool.getContext(adminContextName));
    		
		}
		
		return super.successMsg();
	}

}
