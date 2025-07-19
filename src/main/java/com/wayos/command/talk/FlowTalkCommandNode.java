package com.wayos.command.talk;

import com.wayos.Application;
import com.wayos.ContextListener;
import com.wayos.MessageObject;
import com.wayos.Node;
import com.wayos.NodeEvent;
import com.wayos.Session;
import com.wayos.command.CommandNode;
import com.wayos.command.Key;
import com.wayos.pusher.WebPusher;

import x.org.json.JSONObject;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Wisarut Srisawet on 7/31/2017 AD.
 */
@SuppressWarnings("serial")
public class FlowTalkCommandNode extends CommandNode {

    protected ReadWriteLock lock = new ReentrantReadWriteLock();

    private final Random random = new Random();
    
    private final Key lowConfidenceKey;    
    
    public FlowTalkCommandNode(Session session, Key lowConfidenceKey) {
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
            	
            	//Protect from match the last entry
            	if (session.lastEntry()!=null && 
            			session.lastEntry().node.hasSameId(nodeEvent.node)) {
            		
            		System.err.println("Match Last Entry!!!" + session.getRoundCount());
                	System.err.println(session.lastEntry().node);
                	
            		return;
            	}
            	
            	/**
            	 * TODO: Notify Logic Designer for Advance Debugging
            	 */            	
				WebPusher webPusher = (WebPusher) Application.instance().get("web");
				
		    	String contextName = session.context().name();
		    	
		    	String [] tokens = contextName.split("/");
		    	String toAccountId = tokens[0];
		    	String toBotId = tokens[1];
		    	
		    	String fromSessionId = session.vars("#sessionId");
		    	String targetSessionId = "logic-designer";
		    	
		    	JSONObject data = new JSONObject();
		    	
		    	data.put("type", "nodeId");
		    	data.put("fromAccountId", toAccountId);
		    	data.put("fromBotId", toBotId);
		    	data.put("fromSessionId", fromSessionId);
		    	data.put("nodeId", nodeEvent.node.id());
		    	data.put("message", nodeEvent.messageObject.toString());
		    					
				webPusher.push(contextName, targetSessionId, data);
				
                nodeEvent.node.fast_feed(nodeEvent.messageObject);
                
                activeNodeList.add(nodeEvent.node);

            }
        });
        
        /*
        System.out.println("{");
        System.out.println("\t" + activeNodeList);
        System.out.println("}");
        */
        
        List<Node> maxActiveNodeList = Confidential.maxActiveNodeListFrom(activeNodeList);
        
        final float confidenceRate;
        String responseText;
        Node maxActiveNode;
        
        if ( maxActiveNodeList==null ) {

            maxActiveNode = null;
            confidenceRate = 0.0f;
            responseText = "";

        } else {
        	
            //Could it be Question Menu?
            if ( maxActiveNodeList.size()>1 ) {

                List<Node> questionNodes = new ArrayList<>();
                
                for (Node node:maxActiveNodeList) {
                	
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
                 * Default pickup mode
                 */
                boolean isRandomPickup;
                
                String outputDelimiter = session.context().prop("output.delimiter");
                
                /**
                 * Random Pickup
                 */
                if (outputDelimiter!=null) {
                	
                	boolean isAllLeaf = true;
                	
                	for (Node node:maxActiveNodeList) {
                		
                		if (node.type() != Node.Type.LEAF) {
                			
                			isAllLeaf = false;
                			break;
                		}
                	}
                	
                	if (!isAllLeaf) {
                		
                		isRandomPickup = true;
                		
                	} 
                	                	
                   	/**
                	 * Not has any forwarding or question so that ok to merge all outputs
                	 */
                	else {
                		
                		isRandomPickup = false;
                		
                	}
                	
                } else {        
                	
                	isRandomPickup = true;                	
                }
                
                if (isRandomPickup) {
                	
                    maxActiveNode = maxActiveNodeList.get(random.nextInt(maxActiveNodeList.size()));
                    
                } else {
                	
                	//outputDelimiter = outputDelimiter.replace("[br]", "\n");
                	
                	StringBuilder newResponse = new StringBuilder();
                	for (Node node:maxActiveNodeList) {
                		newResponse.append(node.response());
                		newResponse.append(outputDelimiter);
                	}
                	
                	maxActiveNode = new Node(maxActiveNodeList.get(0));//For active value; all maxActiveNodes have same confidence score.
                	maxActiveNode.setResponse(newResponse.toString().trim());
                	
                }
                
            } else {
            	
            	maxActiveNode = maxActiveNodeList.get(0);
            }            
            
            confidenceRate = maxActiveNode.active();
            responseText = maxActiveNode.response();
                        
        }
        
        /*
        System.out.println("MessageObject:" + messageObject);
        System.out.println("MaxActiveNode:" + confidenceRate + ":" + maxActiveNode);
        System.out.println();
        */
        
        //final float LOWER_BOUND = 0.05f;
        //final float LOWER_BOUND = 0.03f;
        final float LOWER_BOUND = 0;
        
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
            if (messageObject.forwardedFrom(unknownConfig)) {
            	
            	System.err.println("Doubled Forward:" + messageObject);
            	
            	return "";
            	
            }

            if (unknownConfig==null) {
            	
                return "";
                
            } else if (!unknownConfig.endsWith("!")) {
            	
                return unknownConfig;
                
            }
            
            //System.out.println(unknownConfig);
            
            /**
             * Save that message as result
             */
            //messageObject.addResult(messageObject.toString());
            
            
            /**
             * Merge that message as parameters
             */
            //I dont know why i did this!
            //String paramsAdded = "," + unknownConfig.substring(1, unknownConfig.length()-1) + " " + messageObject.toString() + "!";
            
            String paramsAdded = unknownConfig.substring(0, unknownConfig.length()-1) + " " + messageObject.toString() + "!";
            
            //System.out.println(paramsAdded);
            
            /**
             * Replace with UnknownConfig (text after ,)
             */
            messageObject.setText(paramsAdded);
            
            //System.err.println(messageObject.headIncluded());
            
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
