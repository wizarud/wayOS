package com.wayos.command;

import com.wayos.Hook;
import com.wayos.MessageObject;
import com.wayos.Session;

/**
 * Created by Wisarut Srisawet on 7/31/2017 AD.
 */
public class EnableModeCommandNode extends CommandNode {

    public EnableModeCommandNode(Session session, String [] hooks) {
    	
        super(session, hooks, Hook.Match.Head);
        
    }

    @Override
    public String execute(MessageObject messageObject) {

        session.setMode(messageObject.toString());

        return successMsg();
    }
}
