package com.wayos.experiment.command;

import com.wayos.Hook;
import com.wayos.MessageObject;
import com.wayos.Session;
import com.wayos.command.CommandNode;
import com.wayos.command.Key;

/**
 * Created by Wisarut Srisawet on 7/31/2017 AD.
 */
public class ForwardCommandNode extends CommandNode {

    public final Key lowConfidenceKey;

    public ForwardCommandNode(Session session, String [] hooks, Key lowConfidenceKey) {
        super(session, hooks, Hook.Match.All);
        this.lowConfidenceKey = lowConfidenceKey;
    }

    @Override
    public boolean matched(MessageObject messageObject) {
        return super.matched(messageObject) && session.lastEntry() != null;
    }

    @Override
    public String execute(MessageObject messageObject) {

        //Recall
        String response="";

        if (!session.hasProblem()) {
        	
            Session.Entry lastActiveEntry = session.lastEntry();
            response = lastActiveEntry.node.response();
            
            if (!response.equals("?"))
                return new TalkCommandNode(session, lowConfidenceKey).execute(MessageObject.build(messageObject, response));
            
        }

        return response;
    }

}
