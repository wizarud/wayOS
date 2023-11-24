package com.wayos.command.admin;

import java.util.List;

import com.wayos.Application;
import com.wayos.Context;
import com.wayos.MessageObject;
import com.wayos.Node;
import com.wayos.Session;
import com.wayos.Hook.Match;
import com.wayos.command.CommandNode;
import com.wayos.connector.SessionPool;
import com.wayos.drawer.Canvas2D;

@SuppressWarnings("serial")
public class ViewCommandNode extends CommandNode {

	public ViewCommandNode(Session session, String[] hooks, Match match) {
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
	        
	        List<Node> nodeList = canvas2D.query(keywords, Match.All);
	        
	        StringBuilder result = new StringBuilder();
	        
	        for (Node node:nodeList) {
	        	result.append(node.response());
	        	result.append(System.lineSeparator());
	        }
	        
	        return result.toString().trim();
			
		} catch (Exception e) {
			
			throw new RuntimeException(e);
		}
	}

}
