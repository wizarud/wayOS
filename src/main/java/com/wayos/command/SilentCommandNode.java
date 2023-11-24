package com.wayos.command;

import com.wayos.MessageObject;
import com.wayos.Session;

/**
 * Created by eossth on 7/31/2017 AD.
 */
public class SilentCommandNode extends CommandNode {

    public SilentCommandNode(Session session, String [] hooks) {
        super(session, hooks);
    }

    @Override
    public String execute(MessageObject messageObject) {
    	
        if (!session.silent()) {
        	
            session.silent(true);
            return successMsg();
            
        }
        return "";
    }
}
