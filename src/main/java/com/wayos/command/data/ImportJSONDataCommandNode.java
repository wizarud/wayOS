package com.wayos.command.data;

import com.wayos.Hook;
import com.wayos.MessageObject;
import com.wayos.Session;
import com.wayos.command.CommandNode;

/**
 * Created by eossth on 7/31/2017 AD.
 */
public class ImportJSONDataCommandNode extends CommandNode {

    public ImportJSONDataCommandNode(Session session, String [] hooks) {
        super(session, hooks, Hook.Match.Head);
    }

    @Override
    public String execute(MessageObject messageObject) {

        try {

            session.context().loadJSON(cleanHooksFrom(messageObject.toString()));
            session.context().save();

            return successMsg();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return failMsg();
    }

}
