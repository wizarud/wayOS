package com.wayos.command;

import com.wayos.MessageObject;
import com.wayos.Session;
import com.wayos.command.talk.FlowTalkCommandNode;

/**
 * Created by Wisarut Srisawet on 7/31/2017 AD.
 */
public class GreetingCommandNode extends CommandNode {

	public GreetingCommandNode(Session session, String[] hooks) {
		super(session, hooks);
	}

	@Override
    public String execute(MessageObject messageObject) {

        String greetingConfig = session.context().prop("greeting");

        if (greetingConfig==null) {
            return "";
        } else if (!greetingConfig.endsWith("!")) {
            return greetingConfig;
        }

        messageObject.setText(greetingConfig);

        return messageObject.headIncluded() + new FlowTalkCommandNode(session, Key.LEARN).execute(messageObject.forward());
    }
}
