package com.wayos.command;

import com.wayos.MessageObject;
import com.wayos.Session;
import com.wayos.command.talk.FlowTalkCommandNode;

/**
 * Created by Wisarut Srisawet on 7/31/2017 AD.
 */
public class WakeCommandNode extends CommandNode {

    public WakeCommandNode(Session session, String[] hooks) {
		super(session, hooks);
	}

	@Override
    public String execute(MessageObject messageObject) {
    	
        String silentConfig = session.context().prop("silent");
        
        if (silentConfig==null) {
            return "";
        } else if (!silentConfig.endsWith("!")) {
            return silentConfig;
        }

        messageObject.setText(silentConfig);
        
        return messageObject.headIncluded() + new FlowTalkCommandNode(session, Key.LEARN).execute(messageObject.forward());
    }
}
