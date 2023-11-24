package com.wayos.util;

import java.util.TimerTask;

import com.wayos.Session;
import com.wayos.pusher.PusherUtil;

public class SilentPusherTask extends TimerTask {
	
	private SilentPusher silentPusher;
	
	private PusherUtil pusherUtil = new PusherUtil();
	
	private String channel;
	
	private String sessionId;
	
	private String contextName;
	
	private String accountId;
	
	private String botId;
	
	private Session session;
	
	public SilentPusherTask(SilentPusher silentPusher, Session session) {
		
		this.silentPusher = silentPusher;
		
		this.channel = session.vars("#channel");
		
		this.sessionId = session.vars("#sessionId");
		
		this.contextName = session.context().name();
						
		String [] tokens = this.contextName.split("/");
		
		this.accountId = tokens[0];
		
		this.botId = tokens[1];
		
		this.session = session;
		
	}
	
	public String id() {
		
		return contextName + "/" + sessionId;
	}

	@Override
	public void run() {
		
		System.out.println("Do the silent event for " + id());
		
		try {
			
			//Push parsed silent as message to this session
			pusherUtil.parse(accountId, botId, channel, sessionId, "silent");
			
			//Re schedule except the exception such as IllegalArgumentException
			silentPusher.register(session);
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}

	}

}
