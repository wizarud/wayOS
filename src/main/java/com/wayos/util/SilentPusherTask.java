package com.wayos.util;

import java.util.TimerTask;

import com.wayos.Application;
import com.wayos.pusher.PusherUtil;

public class SilentPusherTask extends TimerTask {
	
	private SilentPusher silentPusher;	
	
	private String silentInterval;
	
	private String contextName;
	
	private String channel;
	
	private String sessionId;	
	
	private String accountId;
	
	private String botId;
	
	public SilentPusherTask(SilentPusher silentPusher, String silentInterval, String contextName, String channel, String sessionId) {
		
		this.silentPusher = silentPusher;
		
		this.silentInterval = silentInterval;
		
		this.contextName = contextName;
		
		this.channel = channel;
		
		this.sessionId = sessionId;
		
		String [] tokens = this.contextName.split("/");
		
		this.accountId = tokens[0];
		
		this.botId = tokens[1];
		
	}
	
	public String id() {
		
		return contextName + "/" + sessionId;
	}

	@Override
	public void run() {
		
		System.out.println("Do the silent event for " + id());
		
		try {
			
			PusherUtil pusherUtil = (PusherUtil) Application.instance().get(PusherUtil.class.getName());
			
			//Push parsed silent as message to this session
			pusherUtil.parse(accountId, botId, channel, sessionId, "silent");
			
			//Re schedule except the exception such as IllegalArgumentException
			silentPusher.register(silentInterval, contextName, channel, sessionId, false);
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}

	}

}
