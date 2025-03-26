package com.wayos.command.data;

import com.wayos.MessageObject;
import com.wayos.Session;
import com.wayos.command.CommandNode;

/**
 * Created by Wisarut Srisawet on 7/31/2017 AD.
 */
public class ClearDataCommandNode extends CommandNode {

    public ClearDataCommandNode(Session session, String [] hooks) {
        super(session, hooks);
    }

    @Override
    public String execute(MessageObject messageObject) {

        session.context().clear();
        session.context().save();
        
        try {
        	
            session.context().load();
            
        } catch (Exception e) {
        	
        	return e.getMessage();
        }

        return successMsg();
    }
}
