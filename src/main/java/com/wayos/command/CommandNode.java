package com.wayos.command;

import com.wayos.Application;
import com.wayos.Hook;
import com.wayos.MessageObject;
import com.wayos.Node;
import com.wayos.PathStorage;
import com.wayos.Session;

/**
 * Created by eossth on 7/31/2017 AD.
 */
public abstract class CommandNode extends Node {

    public final Session session;

    public CommandNode(Session session) {
        this (session, null);
    }

    public CommandNode(Session session, String [] hooks) {
        this(session, hooks, Hook.Match.All);
    }

    public CommandNode(Session session, String [] hooks, Hook.Match match) {
        super(Hook.build(hooks, match));
        this.session = session;
    }

    public abstract String execute(MessageObject messageObject);

    protected String successMsg() {
        return "(^o^)ๆ Done!";
    }

    protected String failMsg() {
        return "(T.T)ๆ Fail!";
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
    
	protected final PathStorage storage() {
		
		return Application.instance().get(PathStorage.class);		
	}
    
    
}
