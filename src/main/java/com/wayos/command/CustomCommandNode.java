package com.wayos.command;

import com.wayos.MessageObject;
import com.wayos.NodeEvent;
import com.wayos.Session;
import com.wayos.Hook.Match;

/**
 * Delegate Command to listener implementation
 * @author eoss-th
 *
 */
public class CustomCommandNode extends CommandNode {

	public CustomCommandNode(Session session, String[] hooks, Match match) {
		super(session, hooks, match);
	}

	@Override
	public String execute(MessageObject messageObject) {
		
		if (session.context().listener()!=null) {
			
			NodeEvent nodeEvent = new NodeEvent(this, messageObject, NodeEvent.Event.Custom);
			
			try {
				
				session.context().listener().callback(nodeEvent);
				
			} catch (Exception e) {
				
				throw new RuntimeException(e);
				
			}
			
			if (nodeEvent.getResult()!=null) {
				
				return nodeEvent.getResult();
			}
			
			return super.successMsg();
		}
		
		return "(-.-)ๆ";
	}

}
