package com.wayos.command;

import com.wayos.Session;

import com.wayos.Hook.Match;
import com.wayos.MessageObject;

public class AsyncCommandNode extends CommandNode {
	
	public interface FinishListener {
		
		public void onFinish(AsyncTask.Finish finish);
		
	}
	
	private final AsyncTask.Runner runner;
	
    public AsyncCommandNode(Session session, String [] hooks, AsyncTask.Runner runner) {
    	
        super(session, hooks, Match.Head);
        
        this.runner = runner;
    }
    
	@Override
	public final String execute(MessageObject messageObject) {
		
		try {
			
			System.out.println("Enqueue AsyncCommand..." + messageObject);
			
        	session.asyncTaskList().add(new AsyncTask(this, messageObject.copy(), runner));
						
			return "OK";
	    	
		} catch (Exception e) {
			
			e.printStackTrace();
			
			return e.getMessage();
		}
		
	}
		
			
}

