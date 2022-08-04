package com.wayos.command.data;

import com.wayos.Hook;
import com.wayos.MessageObject;
import com.wayos.Node;
import com.wayos.Session;
import com.wayos.command.CommandNode;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by eossth on 7/31/2017 AD.
 */
public class ImportRawDataCommandNode extends CommandNode {

    public ImportRawDataCommandNode(Session session, String [] hooks) {
        super(session, hooks, Hook.Match.Head);
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

            List<String> sentenceList = new ArrayList<>();
            String line;
            while ((line = br.readLine())!=null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (anotherContextName==null && line.startsWith("#")) {
                    anotherContextName = line.substring(1);
                } else {
                    sentenceList.add(line);
                }
            }

            List<Node> newNodeList = new ArrayList<>();
            Node newNode = null;

            for (String sentence:sentenceList) {

                if (newNode!=null) {
                    newNode.setResponse(sentence);
                    newNodeList.add(newNode);
                }

                newNode = Node.build(session.context().split(sentence));
            }

            /**
             * Merge
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
