package com.wayos.command.talk;

import com.wayos.MessageObject;
import com.wayos.Session;
import com.wayos.command.Key;

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
        
        return messageObject.headIncluded() + new FlowTalkCommandNode(session, Key.LEARN).execute(messageObject.forward().split());
    }
}
