package com.wayos.command.talk;

import com.wayos.ContextListener;
import com.wayos.MessageObject;
import com.wayos.Node;
import com.wayos.NodeEvent;
import com.wayos.Session;
import com.wayos.command.CommandNode;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Wisarut Srisawet on 7/31/2017 AD.
 */
@SuppressWarnings("serial")
public class MultilineInputTalkCommandNode extends CommandNode {

    protected ReadWriteLock lock = new ReentrantReadWriteLock();

    private final Random random = new Random();
    
    private final float lowerBound;
    
    private final int minlinesExecute;
    
    private final String responsesDelimiter;

    public MultilineInputTalkCommandNode(Session session, float lowerBound, int minlinesExecute, String responsesDelimiter) {
        super(session);
        
        this.lowerBound = lowerBound;
        this.minlinesExecute = minlinesExecute;
        this.responsesDelimiter = responsesDelimiter;
    }

    @Override
    public boolean matched(MessageObject messageObject) {
        return true;
    }

    @Override
    public String execute(MessageObject messageObject) {
    	
    	String input = messageObject.toString();
    	
    	/**
    	 * Clean Input
    	 */
    	input = input.replace("`", "").trim();
    	
    	String [] lines = input.split(System.lineSeparator());
    	
    	/**
    	 * Only Support Multilines Input
    	 */
    	if ( lines.length < minlinesExecute ) {
    		
    		return "";
    	}
    	
    	Set<String> resultSet = new HashSet<>();
    	
    	String result;
    	for (String line:lines) {
    		
    		result = executeSingleLine(MessageObject.build(line)).trim();
    		
    		if (!result.isEmpty()) {
    			
    			resultSet.add(result);
    		}
    	}
    	
    	StringBuilder sb = new StringBuilder();
    	
    	for (String r:resultSet) {
    		
    		sb.append(r);
    		sb.append(responsesDelimiter);    		
    	}
    	
        return sb.toString().trim();
    }

	public String executeSingleLine(MessageObject messageObject) {
		
        if (!messageObject.isSplitted()) {   
        	
            messageObject.split();
        }
        
        /**
         * Collect matched nodes and feed them
         */
        final Set<Node> activeNodeSet = new HashSet<>();

        session.context().matched(messageObject, new ContextListener() {
            @Override
            public void callback(NodeEvent nodeEvent) {

            	//Protect from match the last entry
            	if (session.lastEntry()!=null && session.lastEntry().node.hasSameId(nodeEvent.node)) {
            		//System.err.println(session.lastEntry().node);
            		return;
            	}
            	
                nodeEvent.node.feed(messageObject);
                activeNodeSet.add(nodeEvent.node);

            }
        });

        /**
         * Find max active nodes
         */
        List<Node> maxActiveNodes = Confidential.maxActiveNodeListFrom(activeNodeSet);
        
        final float confidenceRate;
        String responseText;
        Node maxActiveNode;
        
        if ( maxActiveNodes==null ) {

            maxActiveNode = null;
            confidenceRate = 0.0f;
            responseText = "";

        } else {
        	
            //Could it be Question Menu?
            if ( maxActiveNodes.size()>1 ) {

                /**
                 * Random Pickup
                 */
                maxActiveNode = maxActiveNodes.get(random.nextInt(maxActiveNodes.size()));
                
            } else {
            	
            	maxActiveNode = maxActiveNodes.get(0);
            }            
            
            confidenceRate = maxActiveNode.active();
            responseText = maxActiveNode.response();
                        
        }
        
        //Low confidence
        if (confidenceRate < lowerBound) {

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
            
            return (messageObject.headIncluded() + executeSingleLine(messageObject.forward(unknownConfig))).trim();
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
            
            Node.Type type = maxActiveNode.type();
            
            ResponseCommandNode responseCommandNode;
            
            if (type==Node.Type.LEAF) {
            	
            	responseCommandNode = new ResponseCommandNode(session, responseText);            	
            	
            } else if (type==Node.Type.FORWARDER) {
            	
            	responseCommandNode = new MultilineInputForwardResponseCommandNode(session, responseText.substring(0, responseText.length()-1), lowerBound, minlinesExecute, responsesDelimiter);
            	
            } else {
            	
            	return "(-.-)?";
            	
            }
            
            return responseCommandNode.execute(forwardMessageObject);
        }

        return responseText;
	}
}
