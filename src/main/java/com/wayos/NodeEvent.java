package com.wayos;

public class NodeEvent {

    public enum Event {
    	
    	//Experiment
        SuperConfidence,
        HesitateConfidence,
        LowConfidence,
        LateReply,
        Question,
        
        Leave,
        RegisterAdmin,
        Recursive,
        Matched,
        Wakeup,
        NewNodeAdded,
        ContextSaved,
        ReservedWords,
        Authentication,
        Custom
    }

    /**
     * Immutable Source Node, for feed calculation
     */
    public final Node node;
    public final Event event;
    public final MessageObject messageObject;
    
    /**
     * Mutable Source Node
     */
    private Node sourceNode;
    
    private String result;

    public NodeEvent(Node node, MessageObject messageObject, Event event) {
        this.node = new Node(node);
        this.sourceNode = node;
        this.messageObject = messageObject;
        this.event = event;
    }
    
    public NodeEvent(MessageObject messageObject, Event event) {
    	this.sourceNode = this.node = null;
        this.messageObject = messageObject;
        this.event = event;
    }

	public Node getSourceNode() {
		return sourceNode;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}
    
}
