package com.wayos.command.data;

import com.wayos.Hook;
import com.wayos.MessageObject;
import com.wayos.Node;
import com.wayos.Session;
import com.wayos.command.CommandNode;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by eossth on 7/31/2017 AD.
 */
@Deprecated
public class ExportMermaidDataCommandNode extends CommandNode {

    public ExportMermaidDataCommandNode(Session session, String [] hooks) {
        super(session, hooks);
    }

    @Override
    public String execute(MessageObject messageObject) {

        String jsonString = toJSONString();

        return jsonString;
    }

    private String test(String jsonString) {
        JSONObject obj = new JSONObject(jsonString);
        JSONArray nodes = obj.getJSONArray("nodes");
        JSONArray arrows = obj.getJSONArray("arrows");

        StringBuilder sb = new StringBuilder("graph LR\n");
        JSONObject node;
        for (int i=0;i<nodes.length();i++) {
            node = nodes.getJSONObject(i);
            sb.append(node.getInt("nodeId"));
            if (node.getString("type").equals("decision")) {
                sb.append("{");
            } else if (node.getString("type").equals("processor")) {
                sb.append("[");
            } else {
                sb.append("(");
            }
            sb.append("\"" + node.getString("hooks") + "<br>" + node.get("response") + "\"");
            if (node.getString("type").equals("decision")) {
                sb.append("}");
            } else if (node.getString("type").equals("processor")) {
                sb.append("]");
            } else {
                sb.append(")");
            }
            sb.append("\n");
        }

        for (int i=0;i<arrows.length();i++) {
            sb.append(arrows.get(i));
            sb.append("\n");
        }

        return sb.toString();
    }

    private String toJSONString() {

        Map<String, String> nodeMap = new TreeMap<>();
        Map<String, List<String>> treeMap = new HashMap<>();
        String hooks, response, forwardHooks;
        List<String> forwardedNodeList;
        for (Node node:session.context().nodeList()) {

            hooks = Hook.toString(node.hookList());
            response = node.response();

            nodeMap.put(hooks, response);

            if (response.endsWith("!") || response.endsWith("?")) {

                response = response.substring(0, response.length()-1);

                Pattern pattern = Pattern.compile("\\`.*?\\`");
                Matcher matcher = pattern.matcher(response);

                String param;
                while (matcher.find()) {
                    param = matcher.group();
                    response = response.replace(param, "");
                }

                pattern = Pattern.compile("#\\d+");
                matcher = pattern.matcher(response);

                while (matcher.find()) {
                    param = matcher.group();
                    response = response.replace(param, "");
                }

                response = response.replace("##", "");

                response = String.join(" ", session.context().split(response));

                response = response.trim();

                for (Node forwardedNode:session.context().nodeList()) {
                    forwardHooks = Hook.toString(forwardedNode.hookList());
                    if (forwardHooks.startsWith(response)) {
                        forwardedNodeList = treeMap.get(hooks);
                        if (forwardedNodeList==null) {
                            forwardedNodeList = new ArrayList<>();
                        }
                        forwardedNodeList.add(forwardHooks);
                        treeMap.put(hooks, forwardedNodeList);
                    }
                }

            }

        }

        //Generate nodeId
        Map<String, Integer> idMap = new HashMap<>();
        Set<String> hookSet = nodeMap.keySet();
        int i = 1;
        for (String hook:hookSet) {
            idMap.put(hook, i);
            i++;
        }

        //Generate JSON
        JSONObject obj = new JSONObject();
        JSONArray entities = new JSONArray();
        JSONArray arrows = new JSONArray();

        JSONObject entity;
        for (Map.Entry<String, String> entry:nodeMap.entrySet()) {

            entity = new JSONObject();

            entity.put("nodeId", idMap.get(entry.getKey()));
            entity.put("hooks", entry.getKey());
            entity.put("response", entry.getValue());

            forwardedNodeList = treeMap.get(entry.getKey());
            if (forwardedNodeList!=null) {

                if (forwardedNodeList.size()>=2) {
                    entity.put("type", "decision");
                } else if (nodeMap.get(entry.getKey()).contains("`")) {
                    entity.put("type", "processor");
                } else {
                    entity.put("type", "end");
                }

                for (String forwardedHooks:forwardedNodeList) {
                    arrows.put(idMap.get(entry.getKey()) + "-->" + idMap.get(forwardedHooks));
                }

            } else {
                if (nodeMap.get(entry.getKey()).contains("`")) {
                    entity.put("type", "processor");
                } else {
                    entity.put("type", "end");
                }
            }

            entities.put(entity);
        }

        obj.put("nodes", entities);
        obj.put("arrows", arrows);

        return obj.toString();
    }

    @Deprecated
    private String _toJSONString() {
        JSONObject obj = new JSONObject();
        JSONArray entities = new JSONArray();
        JSONArray arrows = new JSONArray();

        JSONObject entity;
        int nodeId, forwardedCount;
        String input;
        boolean hasProcessor;
        for (Node node:session.context().nodeList()) {

            nodeId = node.hashCode();
            input = node.response();
            entity = new JSONObject();
            entity.put("nodeId", nodeId);
            entity.put("hooks", Hook.toString(node.hookList()));
            entity.put("response", input);

            if (input.endsWith("!") || input.endsWith("?")) {

                input = input.substring(0, input.length()-1);

                Pattern pattern = Pattern.compile("\\`.*?\\`");
                Matcher matcher = pattern.matcher(input);

                String param;
                hasProcessor = false;
                while (matcher.find()) {
                    param = matcher.group();
                    input = input.replace(param, "");
                    hasProcessor = true;
                }

                pattern = Pattern.compile("#\\d+");
                matcher = pattern.matcher(input);

                while (matcher.find()) {
                    param = matcher.group();
                    input = input.replace(param, "");
                }

                input = input.replace("##", "");

                input = String.join(" ", session.context().split(input));

                input = input.trim();

                forwardedCount = 0;

                for (Node forwardedNode:session.context().nodeList()) {
                    if (Hook.toString(forwardedNode.hookList()).startsWith(input)) {
                        arrows.put(nodeId + "-->" + forwardedNode.hashCode());
//                        arrows.put(Hook.toString(node.hookList()) + ":" +input + "-->" + Hook.toString(forwardedNode.hookList()));
                        forwardedCount ++;
                    }
                }

                if (forwardedCount>=2) {
                    entity.put("type", "decision");
                } else if (hasProcessor) {
                    entity.put("type", "processor");
                } else {
                    entity.put("type", "end");
                }

            } else {

                Pattern pattern = Pattern.compile("\\`.*?\\`");
                Matcher matcher = pattern.matcher(input);
                if (matcher.find()) {
                    entity.put("type", "processor");
                } else {
                    entity.put("type", "end");
                }

            }

            entities.put(entity);
        }

        obj.put("nodes", entities);
        obj.put("arrows", arrows);

        return obj.toString();
    }
}
