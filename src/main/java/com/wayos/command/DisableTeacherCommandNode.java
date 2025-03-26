package com.wayos.command;

import com.wayos.MessageObject;
import com.wayos.Session;

/**
 * Created by Wisarut Srisawet on 7/31/2017 AD.
 */
public class DisableTeacherCommandNode extends CommandNode {

    public DisableTeacherCommandNode(Session session, String [] hooks) {
        super(session, hooks);
    }

    @Override
    public String execute(MessageObject messageObject) {

        session.learning(false);

        return successMsg();
    }
}
