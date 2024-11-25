package com.wayos.command.talk;

import com.wayos.Node;
import com.wayos.Session;

public class ResponseFactoryCommandNode extends ResponseCommandNode {

    public ResponseFactoryCommandNode(Session session, String responseText) {
    	
        super(session, responseText);
    }

    public static ResponseCommandNode build(Session session, Node node) {

        Node.Type type = node.type();
        
        /*
        System.out.println();
        System.out.println("Node: " + node);
        System.out.println("Node Type: " + type);
        System.out.println();
        */        
        
    	String responseText = node.response();
    	
    	if (type==Node.Type.LEAF) {
    		
            return new ResponseCommandNode(session, responseText);
    	}
    	
        if (type==Node.Type.FORWARDER) {
        	
            return new FlowForwardResponseCommandNode(session, responseText.substring(0, responseText.length()-1));
        }
        
        if (type==Node.Type.QUESTIONER) {
        	
            return new FlowQuestionResponseCommandNode(session, responseText.substring(0, responseText.length()-1));
        }
        
        return null;
    }

}
