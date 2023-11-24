package com.wayos.command.talk;

import com.wayos.MessageObject;
import com.wayos.Session;

@SuppressWarnings("serial")
public class FlowQuestionResponseCommandNode extends ResponseCommandNode {

    public FlowQuestionResponseCommandNode(Session session, String responseText) {
        super(session, responseText);
    }

    @Override
    public String execute(MessageObject messageObject) {
        String generatedOutput = super.execute(messageObject);

        messageObject.setText(generatedOutput);

        FlowAnswerResponseCommandNode answerResponseCommandNode = new FlowAnswerResponseCommandNode(session, messageObject.tail());
        session.insert(answerResponseCommandNode);

        return answerResponseCommandNode.createQuestion(messageObject.head()).toString();
    }

}
