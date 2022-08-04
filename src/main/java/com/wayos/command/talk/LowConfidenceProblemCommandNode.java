package com.wayos.command.talk;

import com.wayos.MessageObject;
import com.wayos.Node;
import com.wayos.NodeEvent;
import com.wayos.Session;
import com.wayos.command.Key;

/**
 * Created by eossth on 7/31/2017 AD.
 */
public class LowConfidenceProblemCommandNode extends ProblemCommandNode {

    public final Key key;

    String cancelReason;

    MessageObject problemMessage;

    public LowConfidenceProblemCommandNode(Session session, MessageObject problemMessage, Key key) {

        super(session);
        this.problemMessage = problemMessage;
        this.key = key;
    }

    @Override
    public boolean matched(MessageObject messageObject) {

        try {
            if (key.cancelKeys.contains(messageObject.toString())) {
            	
                cancelReason = key.doneMsg;
                
            } else {
            	
                for (Node protectedFromNode: session.protectedList()) {
                	
                    if (protectedFromNode.matched(messageObject)) {
                    	
                        protectedFromNode.feed(messageObject);
                        cancelReason = protectedFromNode.response();
                        protectedFromNode.release();

                        if (session.sessionListener!=null) {
                        	
                            session.sessionListener.callback(new NodeEvent(this, messageObject, NodeEvent.Event.ReservedWords));
                        }
                        break;
                    }
                    
                }
            }
        } finally {
            session.solved(true);
        }

        return true;
    }

    @Override
    public String execute(MessageObject messageObject) {

        if (cancelReason!=null) return cancelReason;

        Node newNode = session.context().build(problemMessage);
        newNode.setResponse(messageObject.toString());
        session.context().add(newNode);
        
        session.context().save();

        if (session.sessionListener!=null) {
            session.sessionListener.callback(new NodeEvent(newNode, problemMessage, NodeEvent.Event.NewNodeAdded));
        }

        session.setLastEntry(problemMessage, newNode);

        return key.doneMsg;
    }
}
