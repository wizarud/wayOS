package com.wayos.command.admin;

import com.wayos.Application;
import com.wayos.MessageObject;
import com.wayos.Session;
import com.wayos.Hook.Match;
import com.wayos.command.CommandNode;
import com.wayos.connector.SessionPool;

@SuppressWarnings("serial")
public class RunCommandNode extends CommandNode {
	
	public RunCommandNode(Session session, String[] hooks, Match match) {
		super(session, hooks, match);
	}

	@Override
	public String execute(MessageObject messageObject) {
		
		/**
		 * Restore context from session var
		 */
		
		String contextName = session.vars("wayobot.context.name");
		
		SessionPool sessionPool = Application.instance().get(SessionPool.class);
		
		session.context(sessionPool.getContext(contextName));
		
		session.removeVariable("wayobot.context.name");
		session.removeVariable("wayobot.context.admin.name");
		session.fireVariablesChangedEvent();//Store Variables
		
		return super.successMsg();
	}

}
