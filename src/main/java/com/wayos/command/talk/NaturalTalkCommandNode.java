package com.wayos.command.talk;

import com.wayos.ContextListener;
import com.wayos.MessageObject;
import com.wayos.Node;
import com.wayos.NodeEvent;
import com.wayos.Session;
import com.wayos.command.CommandNode;
import com.wayos.command.Key;
import com.wayos.util.SpinnerWheel;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Wisarut Srisawet on 7/31/2017 AD.
 */
@SuppressWarnings("serial")
public class NaturalTalkCommandNode extends CommandNode {

    protected ReadWriteLock lock = new ReentrantReadWriteLock();

    private final Random random = new Random();
    
    private final Key lowConfidenceKey;

    public NaturalTalkCommandNode(Session session, Key lowConfidenceKey) {
        super(session);
        
        this.lowConfidenceKey = lowConfidenceKey;
    }

    @Override
    public boolean matched(MessageObject messageObject) {
        return true;
    }
    
    @Override
    public String execute(MessageObject messageObject) {

        if (!messageObject.isSplitted()) {
        	
            messageObject.split(session.context());
            
        }

        final List<Node> activeNodeList = new ArrayList<>();
                
        session.context().matched(messageObject, new ContextListener() {
            @Override
            public void callback(NodeEvent nodeEvent) {
            	
            	if (session.lastEntry()!=null && session.lastEntry().node.hasSameId(nodeEvent.node)) {
            		return;
            	}
            	
                nodeEvent.node.feed(messageObject);
                activeNodeList.add(nodeEvent.node);
                
            }
        });

        TreeMap<Float, List<Node>> activeNodeMap = Confidential.confidenceActiveNodeListMap(activeNodeList);
                       
        final float confidenceRate;
        String responseText;
        Node maxActiveNode;
        
        if ( activeNodeMap==null ) {

            maxActiveNode = null;
            confidenceRate = 0.0f;
            responseText = "";

        } else {
        	
            /**
             * Those results are contain question or not?
             */
            Boolean hasQuestionNode = false;
                        
        	for (Map.Entry<Float, List<Node>> entry:activeNodeMap.entrySet()) {
        		
        		for (Node node:entry.getValue()) {
        			
                    hasQuestionNode = node.type() == Node.Type.QUESTIONER;
                    
                    if (hasQuestionNode) break;
        		}
        		
                if (hasQuestionNode) break;
        	}
        	
        	if (!hasQuestionNode) {
        		
            	/**
            	 * Random pickup from multi weights
            	 */            	
            	SpinnerWheel<Node> spinnerWheel = new SpinnerWheel<>();
                
            	Float confidence;
            	
            	for (Map.Entry<Float, List<Node>> entry:activeNodeMap.entrySet()) {
            		
            		confidence = entry.getKey();
            		
            		for (Node node:entry.getValue()) {
            			
            			spinnerWheel.add(node, confidence);
            			
            		}
            		
            	}
            	
            	maxActiveNode = spinnerWheel.spin(5);
            	            	
        	} else {
        		
            	/**
            	 * Could it be Question Menu?
            	 */
            	List<Node> maxActiveNodes = activeNodeMap.lastEntry().getValue();
        		
                List<Node> questionNodes = new ArrayList<>();
                
                for (Node node:maxActiveNodes) {
                	
                    if (node.type() == Node.Type.QUESTIONER) {
                    	
                        questionNodes.add(node);
                    }
                }
                
                //Generate Questions
                if (!questionNodes.isEmpty()) {

                    List<Question> questionList = new ArrayList<>();

                    String title, params;
                    for (Node node:questionNodes) {

                        title = node.response();
                        int lastIndexOfComma = title.lastIndexOf(",");
                        if (lastIndexOfComma!=-1) {
                            params = title.substring(lastIndexOfComma + 1, title.length()-1).trim();
                            title = title.substring(0, lastIndexOfComma);
                            questionList.add(new Question(session, title, params));
                        }

                    }
                    
                    if (!questionList.isEmpty()) {
                    	
                    	/**
                    	 * Hotfix for parameter match the keywords; Wait for answer state
                    	 */
                    	if (questionList.size()==1) {
                    		
                            session.insert(new FlowAnswerResponseCommandNode(session, questionList.get(0)));
                    	}
                    	                    	
                    	return Question.toString(questionList);
                    }
                }
                
                /**
                 * Random pickup a single menu.
                 */
                maxActiveNode = maxActiveNodes.get(random.nextInt(maxActiveNodes.size()));
            	
            }
                        
            confidenceRate = maxActiveNode.active();
            responseText = maxActiveNode.response();
                        
        }
        
        final float LOWER_BOUND = 0.05f;
        
        if (session.learning() && confidenceRate <= LOWER_BOUND) {

            responseText = messageObject + " " + lowConfidenceKey.questMsg;
            
            session.insert(new LowConfidenceProblemCommandNode(session, messageObject, lowConfidenceKey));
            
            return responseText;
        }

        //Low confidence
        if (confidenceRate <= LOWER_BOUND) {
        	        	            
            String unknownConfig = session.context().prop("unknown");
                        
            messageObject.attr("unknown", unknownConfig);
            
        	/**
        	 * Stop Recursive
        	 */
            if (messageObject.forwardedFrom(unknownConfig)) return "";

            if (unknownConfig==null) {
            	
                return "";
                
            } else if (!unknownConfig.endsWith("!")) {
            	
                return unknownConfig;
                
            }
            
            /**
             * Save that message as parameter
             */
            messageObject.addResult(messageObject.toString());
            
            /**
             * Replace with UnknownConfig (text after ,)
             */
            messageObject.setText(unknownConfig);
            
            return (messageObject.headIncluded() + execute(messageObject.forward(unknownConfig))).trim();
        }
        
        if (maxActiveNode!=null) {
        	        	
            session.setLastEntry(messageObject, maxActiveNode);

            if (session.reachMaximumRoute()) {

                return "Too many forwarding :(, Please review your graph. [Round=" + session.getRoundCount() + "]";
            }

            //Clean MessageObject
            String input = messageObject.toString();
            StringBuilder forwardInput = new StringBuilder(maxActiveNode.cleanHooksFrom(input));
            MessageObject forwardMessageObject = MessageObject.build(messageObject, forwardInput.toString().trim());
            
            return ResponseFactoryCommandNode.build(session, maxActiveNode).execute(forwardMessageObject);
        }

        return responseText;
    }
}
