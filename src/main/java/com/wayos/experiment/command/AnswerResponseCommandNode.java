package com.wayos.experiment.command;

import com.wayos.MessageObject;
import com.wayos.Session;
import com.wayos.command.Key;
import com.wayos.command.talk.ResponseCommandNode;

public class AnswerResponseCommandNode extends ResponseCommandNode {

    public AnswerResponseCommandNode(Session session, String question) {
        super(session, question);
    }

    @Override
    public boolean matched(MessageObject messageObject) {
        session.solved(true);
        return true;
    }

    @Override
    public String execute(MessageObject messageObject) {
        MessageObject questionMessageObject = MessageObject.build(messageObject, responseText + " " + messageObject.toString());
        questionMessageObject.split();
        return new TalkCommandNode(session, Key.LEARN).execute(questionMessageObject);
    }
}
