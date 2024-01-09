package com.wayos.pusher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.wayos.Application;
import com.wayos.MessageObject;
import com.wayos.Session;
import com.wayos.connector.RequestObject;
import com.wayos.connector.SessionPool;
import com.wayos.util.ConsoleUtil;

import x.org.json.JSONArray;

public class PusherUtil {
	
	private Pusher pusher(String channel) {
		
		return (Pusher) Application.instance().get(channel);
	}
	
	private SessionPool sessionPool() {
		
		return Application.instance().get(SessionPool.class);
	}
	
	private ConsoleUtil consoleUtil() {
		
		return Application.instance().get(ConsoleUtil.class);
	}
	
    public List<String> push(String accountId, String botId, String message) {
    	
        List<String> channelList = Arrays.asList("line", "facebook.page", "web");
        
		List<String> sessionIdList = new ArrayList<>();
		
		for (String channel:channelList) {
			
			sessionIdList.addAll(push(accountId, botId, channel, message));

		}
		        
        return sessionIdList;
    }
        
    public List<String> push(String accountId, String botId, String channel, String message) {
    	
        JSONArray sessionArray = consoleUtil().sessionIdList(accountId, botId, channel);
                
        List<String> sessionIdList = new ArrayList<>();
        for (int i=0; i<sessionArray.length(); i++) {
        	sessionIdList.add(sessionArray.getString(i));
        }
        
        List<String> successIdList = new ArrayList<>();
		for (String sessionId:sessionIdList) {
			
    		try {
    			
    			successIdList.add(push(accountId, botId, channel, sessionId, message));
    			
    		} catch (Exception e) {
    			
    			continue;
    			
    		} 
        	
		}
		
		return successIdList;
    }
    
    public String push(String accountId, String botId, String channel, String sessionId, String message) {
    	
		new Thread(new Runnable() {

			@Override
			public void run() {
				
				try {
					
					String contextName = accountId + "/" + botId;
					
					pusher(channel).push(contextName, sessionId, message);
					
				} catch (Exception e) {
					
					throw new RuntimeException(e);
					
				}
				
			}
			
		}).start();
        
		return sessionId;
    }	
	
    public List<String> parse(String accountId, String botId, String keywords) {
    	
        List<String> channelList = Arrays.asList("line", "facebook.page", "web");
        
		List<String> sessionIdList = new ArrayList<>();
		
		for (String channel:channelList) {
			
			sessionIdList.addAll(parse(accountId, botId, channel, keywords));

		}
		        
        return sessionIdList;
    }
        
    public List<String> parse(String accountId, String botId, String channel, String keywords) {
    	
        JSONArray sessionArray = consoleUtil().sessionIdList(accountId, botId, channel);
                
        List<String> sessionIdList = new ArrayList<>();        
        for (int i=0; i<sessionArray.length(); i++) {
        	sessionIdList.add(sessionArray.getString(i));
        }        
        
        List<String> successIdList = new ArrayList<>();
		for (String sessionId:sessionIdList) {
			
    		try {
    			
    			successIdList.add(parse(accountId, botId, channel, sessionId, keywords));
    			    			
    		} catch (Exception e) {
    			
    			continue;
    			
    		} 
        	
		}
		
		return successIdList;
    }
    
    public String parse(String accountId, String botId, String channel, String sessionId, String keywords) {
    	
    	String contextName = accountId + "/" + botId;
    	
    	Session session = sessionPool().get(RequestObject.create(channel, sessionId, contextName));
    	
    	session.clearProblem();//Clear problem to reset flow
    	
		String response = session.parse(MessageObject.build(keywords));
		
		if (response==null || response.trim().isEmpty()) throw new IllegalArgumentException("Empty result for keyword:" + keywords);
			
		new Thread(new Runnable() {

			@Override
			public void run() {
				
				try {
					
					pusher(channel).push(contextName, sessionId, response);
					
				} catch (Exception e) {
					
					throw new RuntimeException(e + ":" + channel + "/" + sessionId);
					
				}
				
			}
			
		}).start();
		
		return sessionId;
    }

}
