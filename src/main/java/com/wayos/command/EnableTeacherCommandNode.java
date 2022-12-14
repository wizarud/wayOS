package com.wayos.command;

import com.wayos.MessageObject;
import com.wayos.Session;

/**
 * Created by eossth on 7/31/2017 AD.
 */
public class EnableTeacherCommandNode extends CommandNode {

    public EnableTeacherCommandNode(Session session, String [] hooks) {
        super(session, hooks);
    }

    @Override
    public String execute(MessageObject messageObject) {

        session.learning(true);

        return successMsg();
    }
}
