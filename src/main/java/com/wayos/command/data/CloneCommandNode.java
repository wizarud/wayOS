package com.wayos.command.data;

import java.util.HashMap;
import java.util.Map;

import com.wayos.Application;
import com.wayos.Context;
import com.wayos.MessageObject;
import com.wayos.Session;
import com.wayos.Hook.Match;
import com.wayos.command.CommandNode;
import com.wayos.connector.SessionPool;
import com.wayos.drawer.Canvas2D;

@SuppressWarnings("serial")
public class CloneCommandNode extends CommandNode {
	
	public CloneCommandNode(Session session, String[] hooks, Match match) {
		super(session, hooks, match);
	}

	@Override
	public String execute(MessageObject messageObject) {
		
		try {
			
			/**
			 * <keywords>
			 * <param1>=<value1>&
			 * <param2>=<value2>&…
			 */
	        String params = cleanHooksFrom(messageObject.toString());
			
	        String [] tokens = params.split("\\s+", 2);
	        
	        String rootKeywords = tokens[0];
	        
	        String [] paramTokens = tokens[1].split("&");
	        
	        Map<String, String> paramMap = new HashMap<>();
	        
	        String [] keyval;
	        for (int i=0; i<paramTokens.length; i++) {
	        	
	        	keyval = paramTokens[i].split("=");
	        	paramMap.put(keyval[0], keyval[1]);
	        	
	        }
	        
	        Context targetContext;
	        
	        String targetContextName = session.vars("wayobot.context.name");
	        
	        if (targetContextName.isEmpty()) {
	        	
	        	//throw new IllegalArgumentException("Missing session var wayobot.context.name");
	        	
	        	/**
	        	 * For empty target context name, Use itself as target context.
	        	 */
	        	targetContext = session.context();
		        
	        } else {
	        	
	    		SessionPool sessionPool = Application.instance().get(SessionPool.class);
		        targetContext = sessionPool.getContext(targetContextName);
	        }
	        	        
	        Context templateContext = session.context();
	        
			Canvas2D canvas2D = new Canvas2D(targetContext, null, 100, false);
			
			canvas2D.clone(templateContext, rootKeywords, paramMap);
			
			targetContext.save();
			
			targetContext.load();
			
			return super.successMsg();
			
		} catch (Exception e) {
			
			throw new RuntimeException(e);
		}	
	}

}
