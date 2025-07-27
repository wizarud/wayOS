package com.wayos.command;

import com.wayos.Hook.Match;
import com.wayos.MessageObject;
import com.wayos.Session;

public class StopProcessCommandNode extends CommandNode {

    public StopProcessCommandNode(Session session, String [] hooks) {
    	
        super(session, hooks, Match.Head);
        
    }

    @Override
    public String execute(MessageObject messageObject) {
    	
		String params = cleanHooksFrom(messageObject.toString());
				
		try {
			
			long pid = Long.parseLong(params);
			
			ProcessHandle handle = ProcessHandle.of(pid).orElse(null);				
			
			if (handle!=null && handle.isAlive()) {
				
				return "" + handle.destroy();
				
			}
			
			return "Process " + pid + " not found!";
			
	        
		} catch (Exception e) {
			
			e.printStackTrace();
			
			return e.getMessage();
		}
		
    }
}
