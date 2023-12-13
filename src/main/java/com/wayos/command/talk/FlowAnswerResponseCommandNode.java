package com.wayos.command.talk;

import com.wayos.ContextListener;
import com.wayos.MessageObject;
import com.wayos.Node;
import com.wayos.NodeEvent;
import com.wayos.Session;
import com.wayos.command.Key;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 
 * Waiting for answer of messageObject to execute the matched choice
 * 
 * @author Wisarut Srisawet
 *
 */
@SuppressWarnings("serial")
public class FlowAnswerResponseCommandNode extends ResponseCommandNode {

    private Question question;

    public FlowAnswerResponseCommandNode(Session session, String responseText) {
        super(session, responseText);
    }
    
    public FlowAnswerResponseCommandNode(Session session, Question question) {    	
    	super(session, question.toString());
    	
    	this.question = question;
    }

    public Question createQuestion(String title) {
    	
        question = new Question(session, title, responseText);
        
        return question;
    }

    @Override
    public boolean matched(MessageObject messageObject) {
    	
        session.solved(true);
        
        return true;
    }

    @Override
    public String execute(MessageObject messageObject) {

        if (question != null) {

            messageObject.split(session.context());

            /**
             * Find the children nodes with parentIdList 
             */
            List<String> parentIdList = new ArrayList<>();
            List<String> wordList = messageObject.wordList();
            
            for (String word:wordList) {
            	
                if (word.startsWith("@")) {
                	
                    parentIdList.add(word);
                }
            }

            boolean isParent = question.id!=null && parentIdList.contains(question.id);

            if (parentIdList.isEmpty() || isParent) {
            	
                final Set<Node> activeNodeSet = new HashSet<>();
                
                session.context().matched(messageObject, question.nodeList, new ContextListener() {
                    @Override
                    public void callback(NodeEvent nodeEvent) {
                    	
                        nodeEvent.node.feed(messageObject);
                        activeNodeSet.add(nodeEvent.node);
                        
                    }
                });
                                
                List<Node> maxActiveNodes = Confidential.maxActiveNodeListFrom(activeNodeSet);

                //Retry with Default Choices if any
                if (maxActiveNodes==null) {

                    maxActiveNodes = question.defaultChoices;

                }

                if (maxActiveNodes!=null && maxActiveNodes.size()==1) {

                    Node maxActiveNode = maxActiveNodes.get(0);
                    session.setLastEntry(messageObject, maxActiveNode);

                    //Clean MessageObject
                    String input = messageObject.toString();
                    StringBuilder forwardInput = new StringBuilder(maxActiveNode.cleanHooksFrom(input));
                    MessageObject forwardMessageObject = MessageObject.build(messageObject, forwardInput.toString().trim());
                    
                    return ResponseFactoryCommandNode.build(session, maxActiveNode).execute(forwardMessageObject);
                                        
                }
            }

        }
        
        /**
         * Choices Escaped Case
         * Split by space and send back to context
         */

        MessageObject questionMessageObject = MessageObject.build(messageObject, messageObject.toString());
        questionMessageObject.split();
        
        return new FlowTalkCommandNode(session, Key.LEARN).execute(questionMessageObject);
    }
}
