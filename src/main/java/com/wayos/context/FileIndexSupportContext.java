package com.wayos.context;

import com.wayos.Context;
import com.wayos.ContextListener;
import com.wayos.MessageObject;
import com.wayos.Node;
import com.wayos.util.FileStream;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FileIndexSupportContext extends Context {

    private FileContext masterContext;

    private FileStream fileConfig;

    private List<FileContext> supportContextList;

    public FileIndexSupportContext(FileContext context) {
        super(context.name());
        masterContext = context;
        fileConfig = new FileStream(context.getDir(), context.name() + ".index");
        supportContextList = new ArrayList<>();
    }
/*
    @Override
    public Set<Node> feed(MessageObject messageObject) {
        Set<Node> activeNode = masterContext.feed(messageObject);
        for (Context supportContext:supportContextList) {
            activeNode.addAll(supportContext.feed(messageObject));
        }
        return activeNode;
    }

    @Override
    public Set<Node> feed(MessageObject messageObject, float matchedScore) {
        Set<Node> activeNode = masterContext.feed(messageObject, matchedScore);
        for (Context supportContext:supportContextList) {
            activeNode.addAll(supportContext.feed(messageObject, matchedScore));
        }
        return activeNode;
    }
*/
    @Override
    public boolean matched(MessageObject messageObject, ContextListener listener) {

        if (!masterContext.matched(messageObject, listener)) {
            for (Context context: supportContextList) {
                if (context.matched(messageObject, listener)) return true;
            }
        }

        return false;
    }

    @Override
    protected void doLoad(String name) throws Exception {}

    @Override
    protected void doSave(String name, List<Node> nodeList) {}

    @Override
    public void load() throws Exception {
        String [] lines = fileConfig.read().split(System.lineSeparator());

        FileContext context;
        for (String line:lines) {
            if (line.trim().isEmpty()) continue;
            context = new FileContext(fileConfig.getDir(), line);
            supportContextList.add(context);
            context.load();
        }
        masterContext.load();
    }

    @Override
    public void load(String name) throws Exception {
        masterContext.load(name);
    }

    @Override
    public void save() {
        masterContext.save();
    }

    @Override
    public void save(String name) {
        masterContext.save(name);
    }

    @Override
    public void clear() {
        masterContext.clear();
    }

    @Override
    public boolean isEmpty() {
        return masterContext.isEmpty();
    }

    @Override
    public void add(Node newNode) {
        masterContext.add(newNode);
    }

    @Override
    public String toString() {
        return masterContext.toString();
    }

}
