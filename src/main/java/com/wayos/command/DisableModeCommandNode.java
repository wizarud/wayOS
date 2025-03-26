package com.wayos.command;

import com.wayos.MessageObject;
import com.wayos.Session;

/**
 * Created by Wisarut Srisawet on 7/31/2017 AD.
 */
public class DisableModeCommandNode extends CommandNode {

    public DisableModeCommandNode(Session session, String [] hooks) {
        super(session, hooks);
    }

    @Override
    public String execute(MessageObject messageObject) {

        session.clearMode();

        return successMsg();
    }
}
