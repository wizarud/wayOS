package com.wayos.command.data;

import com.wayos.Hook;
import com.wayos.MessageObject;
import com.wayos.Session;
import com.wayos.command.CommandNode;

/**
 * Created by Wisarut Srisawet on 7/31/2017 AD.
 */
public class ExportJSONDataCommandNode extends CommandNode {

    public ExportJSONDataCommandNode(Session session, String [] hooks) {
        super(session, hooks, Hook.Match.Head);
    }

    @Override
    public String execute(MessageObject messageObject) {

        return session.context().toJSONString();
    }

}
