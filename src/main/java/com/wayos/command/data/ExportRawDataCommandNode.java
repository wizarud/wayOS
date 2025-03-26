package com.wayos.command.data;

import com.wayos.Hook;
import com.wayos.MessageObject;
import com.wayos.Node;
import com.wayos.Session;
import com.wayos.command.CommandNode;

/**
 * Created by Wisarut Srisawet on 7/31/2017 AD.
 */
public class ExportRawDataCommandNode extends CommandNode {

    public ExportRawDataCommandNode(Session session, String [] hooks) {
        super(session, hooks);
    }

    @Override
    public String execute(MessageObject messageObject) {

        StringBuilder sb = new StringBuilder();
        String lastInput;
        String lastResponse = null;

        /**
         * A
         * B
         * B
         * C
         * C
         * D
         * ..
         */
        for (Node node:session.context().nodeList()) {
            lastInput = Hook.toString(node.hookList());
            if (lastResponse==null || !Hook.toString(Node.build(session.context().split(lastResponse)).hookList()).equals(lastInput)) {
                sb.append(lastInput);
                sb.append(System.lineSeparator());
            }
            lastResponse = node.response();
            sb.append(lastResponse);
            sb.append(System.lineSeparator());
        }

        return sb.toString().trim();
    }
}
