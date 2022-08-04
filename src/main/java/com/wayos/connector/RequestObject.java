package com.wayos.connector;

import java.io.Serializable;

import com.wayos.MessageObject;
import com.wayos.Session;

public abstract class RequestObject implements Serializable {
	
    public abstract MessageObject messageObject();
    public abstract String sessionId();
    public abstract String contextName();
    
    public static RequestObject create(String sessionId, String contextName) {
    	return new RequestObject() {

			@Override
			public MessageObject messageObject() {
				return MessageObject.build();
			}

			@Override
			public String sessionId() {
				// TODO Auto-generated method stub
				return sessionId;
			}

			@Override
			public String contextName() {
				// TODO Auto-generated method stub
				return contextName;
			}

    	};
    }
    
    public static RequestObject create(String channel, String sessionId, String contextName) {
    	return new RequestObject() {

			@Override
			public MessageObject messageObject() {
				return MessageObject.build().attr("channel", channel);
			}

			@Override
			public String sessionId() {
				// TODO Auto-generated method stub
				return sessionId;
			}

			@Override
			public String contextName() {
				// TODO Auto-generated method stub
				return contextName;
			}

    	};
    }
    
    /**
     * To update session configuration, override here.
     * @param session
     */
    public void prepare(Session session) {
    	
    }
     
}
