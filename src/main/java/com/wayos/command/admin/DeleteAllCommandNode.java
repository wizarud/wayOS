package com.wayos.command.admin;

import com.wayos.Application;
import com.wayos.Context;
import com.wayos.MessageObject;
import com.wayos.Session;
import com.wayos.Hook.Match;
import com.wayos.command.CommandNode;
import com.wayos.connector.SessionPool;
import com.wayos.drawer.Canvas2D;

public class DeleteAllCommandNode extends CommandNode {
	
	public DeleteAllCommandNode(Session session, String[] hooks, Match match) {
		super(session, hooks, match);
	}

	@Override
	public String execute(MessageObject messageObject) {
		
		try {
			
    		SessionPool sessionPool = Application.instance().get(SessionPool.class);
    		
	        Context targetContext = sessionPool.getContext(session.vars("wayobot.context.name"));
	        
			Canvas2D canvas2D = new Canvas2D(targetContext, null, 100, false);
	        
    		canvas2D.removeAll();
    		
			targetContext.save();
			
			targetContext.load();
			
			return super.successMsg();
			
		} catch (Exception e) {
			
			throw new RuntimeException(e);
		}
	}

}
