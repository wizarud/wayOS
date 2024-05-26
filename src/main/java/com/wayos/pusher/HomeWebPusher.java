package com.wayos.pusher;

import javax.websocket.OnOpen;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.Session;

@ServerEndpoint(value = "/")
public class HomeWebPusher {
	
	
	public HomeWebPusher() {}
	
	
	@OnOpen
    public void start(Session session) {
						

    }


    @OnClose
    public void end() {
    	
    }

    @OnMessage
    public void incoming(String message) {
    	
    	
    }	
    
    @OnError
    public void onError(Throwable t) throws Throwable {
    	
    	//t.printStackTrace();
        //remove(this.accountId, this.botId, this.sessionId);
    	
    }
    

    

}
