package com.wayos.command.data;

import com.wayos.MessageObject;
import com.wayos.Session;
import com.wayos.command.CommandNode;
import com.wayos.util.CSVWrapper;

/**
 * Created by eossth on 7/31/2017 AD.
 */
public class ExportCSVDataCommandNode extends CommandNode {
	
	private String delimeter;

    public ExportCSVDataCommandNode(Session session, String [] hooks, String delimeter) {
        super(session, hooks);
        this.delimeter = delimeter;
    }

    @Override
    public String execute(MessageObject messageObject) {
                        
        return new CSVWrapper(session.context(), delimeter).toString();
    }
    
}
