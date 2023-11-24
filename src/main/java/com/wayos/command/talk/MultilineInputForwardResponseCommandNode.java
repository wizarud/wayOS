package com.wayos.command.talk;

import com.wayos.MessageObject;
import com.wayos.Session;

@SuppressWarnings("serial")
public class MultilineInputForwardResponseCommandNode extends ResponseCommandNode {
	
	private final float lowerBound;
	
	private final int minlinesExecute;

    private final String responsesDelimiter;
    
    public MultilineInputForwardResponseCommandNode(Session session, String question, float lowerBound, int minlinesExecute, String responsesDelimiter) {
        super(session, question);
        
        this.lowerBound = lowerBound;
        this.minlinesExecute = minlinesExecute;
        this.responsesDelimiter = responsesDelimiter;
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
        
        return messageObject.headIncluded() + new MultilineInputTalkCommandNode(session, lowerBound, minlinesExecute, responsesDelimiter).executeSingleLine(messageObject.forward().split());
    }
}
