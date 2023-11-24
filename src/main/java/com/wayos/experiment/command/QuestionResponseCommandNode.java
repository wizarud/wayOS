package com.wayos.experiment.command;

import com.wayos.MessageObject;
import com.wayos.Session;
import com.wayos.command.talk.ResponseCommandNode;

public class QuestionResponseCommandNode extends ResponseCommandNode {

    public QuestionResponseCommandNode(Session session, String responseText) {
        super(session, responseText);
    }

    @Override
    public String execute(MessageObject messageObject) {
    	
        String generatedOutput = super.execute(messageObject);

        //Override Question
        String forwardMessage = generatedOutput;
        int lastIndexOfComma = generatedOutput.lastIndexOf(", ");
        if (lastIndexOfComma!=-1 && lastIndexOfComma<generatedOutput.length()-1) {
            forwardMessage = generatedOutput.substring(lastIndexOfComma + 1).trim();
            generatedOutput = generatedOutput.substring(0, lastIndexOfComma);
        }

        AnswerResponseCommandNode answerResponseCommandNode = new AnswerResponseCommandNode(session, forwardMessage);
        session.insert(answerResponseCommandNode);

        return generatedOutput;
    }

}
