package com.wayos.command.talk;

import com.wayos.Hook;
import com.wayos.MessageObject;
import com.wayos.Node;
import com.wayos.Session;
import com.wayos.command.CommandNode;
import com.wayos.command.Key;

/**
 * Created by Wisarut Srisawet on 7/31/2017 AD.
 */
@SuppressWarnings("serial")
public class FeedbackCommandNode extends CommandNode {

    private float feedback;
    private String feedbackResponse;
    private final Key rejectKey;

    public FeedbackCommandNode(Session session, String [] hooks, String feedbackResponse, float feedback) {
        this(session, hooks, feedbackResponse, feedback, null);
    }

    public FeedbackCommandNode(Session session, String [] hooks, String feedbackResponse, float feedback, Key rejectKey) {
        super(session, hooks, Hook.Match.All);
        this.feedbackResponse = feedbackResponse;
        this.feedback = feedback;
        this.rejectKey = rejectKey;
    }

    @Override
    public boolean matched(MessageObject messageObject) {
        return super.matched(messageObject);
    }

    @Override
    public String execute(MessageObject messageObject) {

        Session.Entry lastActiveEntry = session.lastEntry();

        if (lastActiveEntry==null) return messageObject.toString();

        Node targetNode = session.context().get(lastActiveEntry.node.hookList());

        if (targetNode==null) return messageObject.toString();

        if (rejectKey!=null) {

            if (session.learning()) {
            	
                session.insert(new RejectProblemCommandNode(session, lastActiveEntry, rejectKey));
                
                feedbackResponse = lastActiveEntry.messageObject.toString().trim() + " " + rejectKey.questMsg;
                
            } else {
            	
                targetNode.feedback(lastActiveEntry.messageObject, feedback);
                
            }

        } else if (feedback > 0) {

            Node newNode = session.context().build(lastActiveEntry.messageObject);

            if (session.learning() && !targetNode.coverHooks(newNode)) {
            	
                targetNode.addHook(newNode);
                
            } else {
            	
                targetNode.feedback(lastActiveEntry.messageObject, feedback);

            }

        }

        session.clearLastEntry();

        session.context().save();

        return feedbackResponse;
    }
}
