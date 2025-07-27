package com.wayos.command;

import com.wayos.MessageObject;
import com.wayos.Session;
import com.wayos.Hook.Match;

public class WaitCommandNode extends CommandNode {

    public WaitCommandNode(Session session, String [] hooks) {
    	
        super(session, hooks, Match.Head);
        
    }

    @Override
    public String execute(MessageObject messageObject) {
    	
		String params = cleanHooksFrom(messageObject.toString());
		
		try {
			
			long ms = Long.parseLong(params);
			
			Thread.sleep(ms);						
	        
		} catch (Exception e) {
			
			e.printStackTrace();
			
			return e.getMessage();
		}
		
		return "done";
		
	}
}
