package com.wayos.command.data;

import com.wayos.Hook;
import com.wayos.MessageObject;
import com.wayos.Node;
import com.wayos.Session;
import com.wayos.command.CommandNode;

/**
 * Created by Wisarut Srisawet on 7/31/2017 AD.
 */
public class ExportQADataCommandNode extends CommandNode {

    public final String qKey;

    public final String aKey;

    public ExportQADataCommandNode(Session session, String [] hooks, String qKey, String aKey) {
        super(session, hooks);
        this.qKey = qKey;
        this.aKey = aKey;
    }

    @Override
    public String execute(MessageObject messageObject) {

        StringBuilder sb = new StringBuilder();
        for (Node node:session.context().nodeList()) {
            sb.append(qKey);
            sb.append(Hook.toString(node.hookList()));
            sb.append(System.lineSeparator());
            sb.append(aKey);
            sb.append(node.response());
            sb.append(System.lineSeparator());
        }

        return sb.toString().trim();
    }
}
