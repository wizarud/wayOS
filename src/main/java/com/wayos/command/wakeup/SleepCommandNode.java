package com.wayos.command.wakeup;

import com.wayos.MessageObject;
import com.wayos.MessageTemplate;
import com.wayos.Session;
import com.wayos.command.CommandNode;

/**
 * Created by Wisarut Srisawet on 7/31/2017 AD.
 */
public class SleepCommandNode extends CommandNode {

    private final CommandNode wakeupCommandNode;

    public SleepCommandNode(Session session, String [] hooks, CommandNode wakeupCommandNode) {
        super(session, hooks);
        this.wakeupCommandNode = wakeupCommandNode;
    }

    @Override
    public String execute(MessageObject messageObject) {
    	
        session.commandList().clear();
        session.commandList().add(wakeupCommandNode);
        return MessageTemplate.STICKER + "1:1";
        
    }
}
