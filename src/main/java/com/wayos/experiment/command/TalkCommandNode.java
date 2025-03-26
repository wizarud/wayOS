package com.wayos.experiment.command;

import com.wayos.ContextListener;
import com.wayos.MessageObject;
import com.wayos.Node;
import com.wayos.NodeEvent;
import com.wayos.Session;
import com.wayos.command.CommandNode;
import com.wayos.command.Key;
import com.wayos.command.talk.Confidential;
import com.wayos.command.talk.LowConfidenceProblemCommandNode;
import com.wayos.command.talk.ResponseFactoryCommandNode;

import java.util.*;

/**
 * Created by Wisarut Srisawet on 7/31/2017 AD.
 */
@SuppressWarnings("serial")
public class TalkCommandNode extends CommandNode {

    private final Key lowConfidenceKey;
    
    private final Random random = new Random();

    public TalkCommandNode(Session session, Key lowConfidenceKey) {
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

        final Set<Node> activeNodeSet = new HashSet<>();

        List<Node> alreadyRoutedNodeList = new ArrayList<>();

        session.context().matched(messageObject, new ContextListener() {
        	
            @Override
            public void callback(NodeEvent nodeEvent) {
            	
                /**
                 * Protect from Cyclic Forwarding
                 */
                if (session.reachMaximumRoute()==false) {
                	
                    nodeEvent.node.feed(messageObject);
                    activeNodeSet.add(nodeEvent.node);
                    
                } else {
                	
                    alreadyRoutedNodeList.add(nodeEvent.node);
                    
                }
            }
        });

        Node maxActiveNode = Confidential.maxActiveNodeFrom(activeNodeSet, random);

        final float confidenceRate;
        String responseText;
        if (maxActiveNode==null) {
        	
            confidenceRate = 0.0f;
            responseText = "";
            
        } else {
        	
            confidenceRate = maxActiveNode.active();
            responseText = maxActiveNode.response();
            
        }

        final float UPPER_BOUND = 0.5f;
        final float LOWER_BOUND = 0.05f;

        if (session.learning() && confidenceRate <= LOWER_BOUND) {

            responseText = messageObject + " " + lowConfidenceKey.questMsg;
            session.insert(new LowConfidenceProblemCommandNode(session, messageObject, lowConfidenceKey));
            
            return responseText;
        }

        //Super Confidence
        if (confidenceRate > 1) {

            if (session.sessionListener != null) {
            	
                session.sessionListener.callback(new NodeEvent(maxActiveNode, messageObject, NodeEvent.Event.SuperConfidence));
            }

        } else if (confidenceRate > UPPER_BOUND) {

            //Nothing TO DO; Just Answer

        } else if (confidenceRate > LOWER_BOUND) {

            //hesitation
            if (session.sessionListener != null) {
                session.sessionListener.callback(new NodeEvent(maxActiveNode, messageObject, NodeEvent.Event.HesitateConfidence));
            }

        } else if (!alreadyRoutedNodeList.isEmpty()) {

            responseText = session.lastEntry().node.response();

        } else {

            responseText = "";
            if (session.sessionListener!=null) {
            	
                //Warning! maxActiveNode may be null
                session.sessionListener.callback(new NodeEvent(maxActiveNode, messageObject, NodeEvent.Event.LowConfidence));
            }

        }

        if (maxActiveNode!=null) {

            session.setLastEntry(messageObject, maxActiveNode);

            //Clean MessageObject
            String input = messageObject.toString();
            StringBuilder forwardInput = new StringBuilder(maxActiveNode.cleanHooksFrom(input));
            MessageObject forwardMessageObject = MessageObject.build(messageObject, forwardInput.toString().trim());
            
            return ResponseFactoryCommandNode.build(session, maxActiveNode).execute(forwardMessageObject);
        }

        return responseText;
    }
}
