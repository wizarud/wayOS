package com.wayos.command;

import com.wayos.Hook;
import com.wayos.MessageObject;
import com.wayos.NodeEvent;
import com.wayos.Session;

/**
 * Created by Wisarut Srisawet on 7/31/2017 AD.
 */
public class LeaveCommandNode extends CommandNode {

    public final String leaveMsg;

    public LeaveCommandNode(Session session, String [] hooks, String leaveMsg) {
        super(session, hooks, Hook.Match.All);
        this.leaveMsg = leaveMsg;
    }

    @Override
    public String execute(MessageObject messageObject) {

        if (session.sessionListener !=null) {
            session.sessionListener.callback(
                    new NodeEvent(this,
                            MessageObject.build(messageObject,
                                    leaveMsg),
                            NodeEvent.Event.Leave));
        }

        return "";
    }
}
