package com.wayos.command.admin;

import com.wayos.Application;
import com.wayos.Context;
import com.wayos.MessageObject;
import com.wayos.Session;
import com.wayos.Hook.Match;
import com.wayos.command.CommandNode;
import com.wayos.connector.SessionPool;
import com.wayos.drawer.Canvas2D;

@SuppressWarnings("serial")
public class DeleteCommandNode extends CommandNode {
	
	public DeleteCommandNode(Session session, String[] hooks, Match match) {
		super(session, hooks, match);
	}

	@Override
	public String execute(MessageObject messageObject) {
		
		try {
			
			/**
			 * <keywords>
			 */
	        String keywords = cleanHooksFrom(messageObject.toString());
    		
    		SessionPool sessionPool = Application.instance().get(SessionPool.class);
    		
	        Context targetContext = sessionPool.getContext(session.vars("wayobot.context.name"));
	        
			Canvas2D canvas2D = new Canvas2D(targetContext, null, 100, false);
	        
    		int matchRootKeywordsCount = canvas2D.remove(keywords);
    		
			targetContext.save();
			
			targetContext.load();
			
			if (matchRootKeywordsCount > 0) {
				
				return super.successMsg();
			}
			
			return super.failMsg() + " " + keywords + " Not Found!";
			
		} catch (Exception e) {
			
			throw new RuntimeException(e);
		}
	}

}
