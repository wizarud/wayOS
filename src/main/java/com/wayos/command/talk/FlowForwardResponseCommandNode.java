package com.wayos.command.talk;

import java.util.Set;

import com.wayos.Application;
import com.wayos.MessageObject;
import com.wayos.Session;
import com.wayos.command.AsyncTask;
import com.wayos.command.Key;
import com.wayos.pusher.WebPusher;

public class FlowForwardResponseCommandNode extends ResponseCommandNode {

    public FlowForwardResponseCommandNode(Session session, String resonseText) {
        super(session, resonseText);
    }

    @Override
    public boolean matched(MessageObject messageObject) {
        session.solved(true);
        return true;
    }

    @Override
    public String execute(MessageObject messageObject) {

        String generatedOutput = super.execute(messageObject);
        
        messageObject.setText(generatedOutput);
        
        String head = messageObject.headIncluded();
    	
    	MessageObject forwardMessage = messageObject.forward().split();
    	
    	//System.out.println("\t" +head + "\t" + forwardMessage);    	
    	
    	//Suspense flow, continue forwardMessage after finish async commands
        if (session.asyncTaskList().size() > 0) {
        	
        	return head;
        }
                    	
        return head + new FlowTalkCommandNode(session, Key.LEARN).execute(forwardMessage);
        
        //return messageObject.headIncluded() + new FlowTalkCommandNode(session, Key.LEARN).execute(messageObject.forward().split());
    }
    
	@Override
	public void onFinish(AsyncTask.Finish finish) {
		
		super.onFinish(finish);
				
		if (session.asyncTaskList().isEmpty()) {
			
			String generatedOutput = (String) finish.val("generatedOutput");
			
			if (generatedOutput!=null && !generatedOutput.isEmpty()) {
				
				/**
				 * TODO: Attach parameters #<tag1>..#<tagN>
				 */
				String tags = (String) finish.val("tags");
				if (tags!=null) {
					tags = tags.replace("#", "");
					System.out.println("\t" + tags);
					generatedOutput += " " + tags;
				}
				
				MessageObject nextMessage = MessageObject.build(generatedOutput).forward().split();
				
				System.out.println("Async Finish! on forwarding flow: " + generatedOutput);
				
				System.out.println("Post this: " + nextMessage);
								
				WebPusher webPusher = (WebPusher) Application.instance().get("web");
				
				String contextName = session.context().name();
				
				String [] tokens = contextName.split("/");
				
				String accountId = tokens[0];
				
				String botId = tokens[1];							
				
				String sessionId = session.vars("#sessionId");
				    						
		    	WebPusher.send(accountId, botId, sessionId, nextMessage.toString(), "forward");				
			}
			
		} 
	}
}
