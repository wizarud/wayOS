package com.wayos.command;

import com.wayos.MessageObject;
import com.wayos.Session;

/**
 * Created by eossth on 7/31/2017 AD.
 */
public class GetPropertyCommandNode extends CommandNode {

    public GetPropertyCommandNode(Session session, String [] hooks) {
        super(session, hooks);
    }

    @Override
    public String execute(MessageObject messageObject) {

		String propertyValue = session.context().prop(messageObject.toString());
		
		return propertyValue;
    }
}
