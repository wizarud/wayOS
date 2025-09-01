package com.wayos.command;

import com.wayos.MessageObject;
import com.wayos.command.AsyncTask.Finish;

/**
 * Upgrade CommandNode to Run as Async!
 */
public class AsyncCommandRunner extends AsyncTask.Runner {
	
	private CommandNode commandNode;
	
	public AsyncCommandRunner(CommandNode commandNode) {
		
		this.commandNode = commandNode;
		
	}
	
	@Override
	public void run(MessageObject messageObject) {
				
		AsyncCommandNode asyncCommandNode = getAsyncCommandNode();
		
		try {
						
			if (!isActive()) return;
			
			//String params = asyncCommandNode.cleanHooksFrom(messageObject.toString());
			
			//String sessionId = asyncCommandNode.session.vars("#sessionId");
						
			commandNode.execute(messageObject);
			
			finish(Finish.SUCCESS);		
			 
		} catch (Exception e) {
						
			finish(Finish.ERROR);
		}
				
	}

}

