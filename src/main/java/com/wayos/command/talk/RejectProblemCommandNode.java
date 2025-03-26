package com.wayos.command.talk;

import com.wayos.MessageObject;
import com.wayos.Node;
import com.wayos.NodeEvent;
import com.wayos.Session;
import com.wayos.command.Key;

/**
 * Created by Wisarut Srisawet on 7/31/2017 AD.
 */
public class RejectProblemCommandNode extends ProblemCommandNode {

    public final Key rejectKey;

    String cancelReason;

    Session.Entry problemEntry;

    RejectProblemCommandNode(Session session, Session.Entry problemEntry, Key rejectKey) {

        super(session);
        this.problemEntry = problemEntry;
        this.rejectKey = rejectKey;
    }

    @Override
    public boolean matched(MessageObject messageObject) {

        try {
        	
            if (rejectKey.cancelKeys.contains(messageObject.toString())) {
            	
                cancelReason = rejectKey.doneMsg;
                
            } else {
            	
                for (Node protectedFromNode: session.protectedList()) {
                	
                    if (protectedFromNode.matched(messageObject)) {
                    	
                        protectedFromNode.feed(messageObject);
                        cancelReason = protectedFromNode.response();
                        protectedFromNode.release();
                        if (session.sessionListener !=null) {
                        	
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

        Node newNode = session.context().build(problemEntry.messageObject);

        //Same Hook; just change the response
        if (newNode.sameHooks(problemEntry.node)) {

            newNode = session.context().get(newNode.hookList());
            newNode.setResponse(messageObject.toString());

        } else {

            newNode.setResponse(messageObject.toString());
            session.context().add(newNode);

            if (session.sessionListener!=null) {
            	
                session.sessionListener.callback(new NodeEvent(newNode, problemEntry.messageObject, NodeEvent.Event.NewNodeAdded));
            }
        }

        session.context().save();

        session.setLastEntry(problemEntry.messageObject, newNode);

        return rejectKey.doneMsg;
    }
}
