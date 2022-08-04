package com.wayos.command.data;

import com.wayos.MessageObject;
import com.wayos.Session;
import com.wayos.command.CommandNode;

/**
 * Created by eossth on 7/31/2017 AD.
 */
public class SaveDataCommandNode extends CommandNode {

    public SaveDataCommandNode(Session session, String [] hooks) {
        super(session, hooks);
    }

    @Override
    public String execute(MessageObject messageObject) {

        try {
            session.context().save();
            return successMsg();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return failMsg();
    }
}
