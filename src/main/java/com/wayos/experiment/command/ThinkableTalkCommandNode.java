package com.wayos.experiment.command;

import com.wayos.Context;
import com.wayos.ContextListener;
import com.wayos.MessageObject;
import com.wayos.Node;
import com.wayos.NodeEvent;
import com.wayos.Session;
import com.wayos.command.CommandNode;
import com.wayos.command.Key;
import com.wayos.command.talk.Confidential;
import com.wayos.command.talk.LowConfidenceProblemCommandNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Wisarut Srisawet on 7/31/2017 AD.
 */
@SuppressWarnings("serial")
public class ThinkableTalkCommandNode extends CommandNode {

    private final Key lowConfidenceKey;

    private final Set<Node> activeNodePool = new HashSet<>();
    
    public ThinkableTalkCommandNode(Session session, Key lowConfidenceKey) {
        super(session);
        this.lowConfidenceKey = lowConfidenceKey;
    }

    @Override
    public boolean matched(MessageObject messageObject) {
    	
        return true;
    }
    
    public void merge(Set<Node> newActiveNodeSet) {
    	
        for (Node newActiveNode:newActiveNodeSet) {
        	
            if (!activeNodePool.add(newActiveNode)) {
            	
                activeNodePool.remove(newActiveNode);
                activeNodePool.add(newActiveNode);
                
            }
            
        }
    }

    public void release(float rate) {

        Set<Node> deadList = new HashSet<>();

        for (Node activeNode:activeNodePool) {
            activeNode.release(rate);
            if (activeNode.active()<0.25f) {
                deadList.add(activeNode);
            }
        }
        
        for (Node deadNode:deadList) {
            deadNode.release();
            activeNodePool.remove(deadNode);
        }
    }

    public void clearPool() {
    	
        for (Node activeNode: activeNodePool) {
            activeNode.release();
        }
        
        activeNodePool.clear();
    }

    private Set<Node> think(final MessageObject messageObject) {

        messageObject.attr("wordCount", session.context().split(messageObject.toString()).length);

        final Set<Node> activeNodeSet = new HashSet<>();

        //Feed Session's nodes
        session.context().matched(messageObject, new ArrayList<>(activeNodePool), new ContextListener() {
            @Override
            public void callback(NodeEvent nodeEvent) {

                nodeEvent.node.feed(messageObject);

                if (!activeNodeSet.add(nodeEvent.node)) {
                    activeNodeSet.remove(nodeEvent.node);
                    activeNodeSet.add(nodeEvent.node);
                }
            }
        });

        //Not found!Fetch from Context
        if (activeNodeSet.isEmpty()) {

            session.context().matched(messageObject, new ContextListener() {
                @Override
                public void callback(NodeEvent nodeEvent) {
                    nodeEvent.node.feed(messageObject);
                    activeNodeSet.add(nodeEvent.node);
                }
            });

        }

        return activeNodeSet;
    }

    @Override
    public String execute(MessageObject messageObject) {

        Set<Node> activeNodeSet = think(messageObject);

        Node maxActiveNode = Confidential.maxActiveNodeFrom(activeNodeSet);

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
            clearPool();
            
            return responseText;
        }

        //Super Confidence
        if (confidenceRate >= 1) {

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

        } else {

            responseText = "";
            if (session.sessionListener!=null) {
                //Warning! maxActiveNode may be null
                session.sessionListener.callback(new NodeEvent(maxActiveNode, messageObject, NodeEvent.Event.LowConfidence));
            }

        }

        if (confidenceRate >= 0.75) {
        	
            clearPool();
            
        } else {
        	
            merge(activeNodeSet);
            merge(think(MessageObject.build(messageObject, responseText)));
            release(0.5f);
            
        }

        if (maxActiveNode!=null) {

            session.setLastEntry(messageObject, maxActiveNode);
        }

        return responseText;
    }
}
