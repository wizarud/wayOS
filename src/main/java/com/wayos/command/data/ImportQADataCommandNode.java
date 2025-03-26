package com.wayos.command.data;

import com.wayos.Hook;
import com.wayos.MessageObject;
import com.wayos.Node;
import com.wayos.Session;
import com.wayos.command.CommandNode;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.*;

/**
 * Created by Wisarut Srisawet on 7/31/2017 AD.
 */
public class ImportQADataCommandNode extends CommandNode {

    public final String qKey;

    public final String aKey;

    public ImportQADataCommandNode(Session session, String [] hooks, String qKey, String aKey) {
        super(session, hooks, Hook.Match.Head);
        this.qKey = qKey;
        this.aKey = aKey;
    }

    @Override
    public String execute(MessageObject messageObject) {

        try {

            String anotherContextName = load(MessageObject.build(messageObject, cleanHooksFrom(messageObject.toString())));

            if (anotherContextName==null)
                session.context().save();
            else
                session.context().save(anotherContextName);

            return successMsg();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return failMsg();
    }

    String load(MessageObject messageObject) throws Exception {

        String anotherContextName = null;
        StringReader reader = new StringReader(messageObject.toString());
        BufferedReader br = new BufferedReader(reader);
        try {
            String line;
            StringBuilder sb = new StringBuilder();
            Node newNode = null;
            List<Node> newNodeList = new ArrayList<>();
            while (true) {
                line = br.readLine();
                if (line==null) break;
                line = line.trim();
                if (line.isEmpty()) continue;
                if (anotherContextName==null && line.startsWith("#")) {
                    anotherContextName = line.substring(1);
                }
                if (line.startsWith(qKey)) {
                    if (!sb.toString().trim().isEmpty()) {
                        if (newNode!=null)
                            newNode.setResponse(sb.toString().trim());
                    }
                    sb = new StringBuilder(line.replace(qKey, ""));
                } else if (line.startsWith(aKey)) {
                    if (!sb.toString().trim().isEmpty()) {
                        newNode = Node.build(session.context().split(sb.toString().trim()));
                        newNodeList.add(newNode);
                    }
                    sb = new StringBuilder(line.replace(aKey, ""));
                } else {
                    sb.append(line+System.lineSeparator());
                }
            }

            if (!sb.toString().isEmpty()) {
                if (newNode!=null)
                    newNode.setResponse(sb.toString().trim());
            }

            /**
             * Merge
             *
             * Current = [A, B]
             * New = [B, C]
             * Remove = [A]
             * Merged = [B]
             * Merged = [B, C]
             * Add [B, C]
             *
             *
             */
            List<Node> mergedNodeList = new ArrayList<>(session.context().nodeList());

            //Create remove node set
            Set<Node> removeNodeSet = new HashSet<>();
            for (Node oldNode:mergedNodeList) {
                if (!newNodeList.contains(oldNode)) {
                    removeNodeSet.add(oldNode);
                }
            }

            //Remove old node
            for (Node removeNode:removeNodeSet) {
                mergedNodeList.remove(removeNode);
            }

            //Add new node
            for (Node node:newNodeList) {
                if (!mergedNodeList.contains(node)) {
                    mergedNodeList.add(node);
                }
            }

            session.context().nodeList().clear();
            session.context().nodeList().addAll(mergedNodeList);

        } finally {
            try { br.close(); } catch (Exception e) {}
        }

        return anotherContextName;
    }
}
