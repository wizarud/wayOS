package com.wayos.command;

import com.wayos.MessageObject;
import com.wayos.Session;
import com.wayos.Hook.Match;

public class EchoCommandNode extends CommandNode {

	public EchoCommandNode(Session session, String[] hooks) {
		super(session, hooks);
	}
	
	public EchoCommandNode(Session session, String[] hooks, Match match) {
		super(session, hooks, match);
	}

	@Override
	public String execute(MessageObject messageObject) {
		
		return cleanHooksFrom(messageObject.toString());
	}

}
