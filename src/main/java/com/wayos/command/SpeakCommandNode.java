package com.wayos.command;

import com.wayos.MessageObject;
import com.wayos.Session;

/**
 * Created by eossth on 7/31/2017 AD.
 */
@SuppressWarnings("serial")
public class SpeakCommandNode extends CommandNode {

    public SpeakCommandNode(Session session, String [] hooks) {
        super(session, hooks);
    }

    @Override
    public String execute(MessageObject messageObject) {
    	
        if (session.silent()) {
        	
            session.silent(false);
            return successMsg();
        }
        
        return "";
    }
}
