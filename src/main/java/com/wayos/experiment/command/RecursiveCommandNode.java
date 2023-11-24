package com.wayos.experiment.command;

import com.wayos.MessageObject;
import com.wayos.NodeEvent;
import com.wayos.command.CommandNode;
import com.wayos.command.admin.AdminCommandNode;
import com.wayos.command.talk.LowConfidenceProblemCommandNode;
import com.wayos.command.talk.RejectProblemCommandNode;
import com.wayos.command.web.GetCommandNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by eossth on 7/31/2017 AD.
 */
public class RecursiveCommandNode extends CommandNode {

    private CommandNode commandNode;

    private List<Class<? extends CommandNode>> ignoreCommandList = new ArrayList<>(
            Arrays.asList(
                    RecursiveCommandNode.class,
                    LowConfidenceProblemCommandNode.class,
                    RejectProblemCommandNode.class,
                    GetCommandNode.class,
                    AdminCommandNode.class));

    public RecursiveCommandNode(CommandNode commandNode) {
        this(commandNode, null);
    }

    public RecursiveCommandNode(CommandNode commandNode, List<Class<? extends CommandNode>> ignoreCommandList) {

        super(commandNode.session);
        this.commandNode = commandNode;

        if (ignoreCommandList!=null) {
            this.ignoreCommandList.addAll(ignoreCommandList);
        }
    }

    public String execute(MessageObject messageObject) {

        String response = commandNode.execute(messageObject);

        if (!session.hasProblem()) {

            String recursiveResponse = null;
            for (CommandNode c: session.commandList()) {
                if (ignoreCommandList.contains(c.getClass())) continue;
                if (c.matched(MessageObject.build(messageObject, response))) {

                    recursiveResponse = c.execute(
                            MessageObject.build(messageObject,
                                    session.lastEntry().node.cleanHooksFrom(messageObject.toString())));
                    break;
                }
            }

            if (recursiveResponse!=null) {
                if (session.sessionListener !=null) {
                    session.sessionListener.callback(
                            new NodeEvent(this,
                                    MessageObject.build(messageObject, recursiveResponse),
                                    NodeEvent.Event.Recursive));
                }
            }
        }

        return response;
    }

    @Override
    public boolean matched(MessageObject messageObject) {

        return commandNode.matched(messageObject);
    }

}
