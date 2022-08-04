package com.wayos.pusher;

import com.wayos.PathStorage;

public abstract class Pusher {
	
	protected final PathStorage storage;
	
	protected Pusher(PathStorage storage) {
		
		this.storage = storage;
	}

	public abstract void push(String contextName, String sessionId, String message);

}
