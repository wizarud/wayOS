package com.wayos.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Timer;

import com.wayos.PathStorage;
import com.wayos.Session;

import x.org.json.JSONObject;

public class SilentPusher {
	
	private Map<String, SilentPusherTask> silentTaskMap = new HashMap<>();
	
	private PathStorage storage;
	
	public SilentPusher(PathStorage storage) {
		
		this.storage = storage;
		
	}
	
	public void register(Session session, String resultOfSilent) {
		
		/**
		 * For silent task
		 */	
		String silentInterval = session.context().prop("SILENT_INTERVAL");
		String contextName = session.context().name();
		String channel = session.vars("#channel"); 
		String sessionId = session.vars("#sessionId");
				
		if (resultOfSilent.trim().isEmpty()) {
			
			/**
			 * Delete old task if exits
			 */
			System.out.println("Deactivate Task: " + "silent/" + contextName.replace("/", ".") + "." + channel + "." + sessionId);
			
			storage.delete("silent/" + contextName.replace("/", ".") + "." + channel + "." + sessionId);
			
			return;
		}
		
		register(silentInterval, contextName, channel, sessionId, false);

	}
	
	public void register(String silentInterval, String contextName, String channel, String sessionId, boolean run) {
		
		SilentPusherTask silentPusherTask = new SilentPusherTask(this, silentInterval, contextName, channel, sessionId);
		
		if (run) {
			
			silentPusherTask.run();
		}
		
		String id = silentPusherTask.id();
		
		//Cancel if there is any pending task.
		remove(id);
		
		silentTaskMap.put(id, silentPusherTask);
		
		Timer timer = new Timer("Silent Timer");
				
		long interval;
		
		try {
			
			interval = Long.parseLong(silentInterval);//Every Hours
			
		    timer.schedule(silentPusherTask, interval * 60 * 60 * 1000);
		    
		} catch (Exception e4) {
			
			Random random = new Random();
			
			interval = (random.nextInt(25) + 1); //Random between 1-24 Hours
			
		    timer.schedule(silentPusherTask, interval * 60 * 60 * 1000);
		    
		}		

	    /**
	     * Save Task Name
	     */
		System.out.println("Save scheduled task: " + silentPusherTask.id() + " every " + interval + " hours");
		
		JSONObject silentObj = new JSONObject();
		silentObj.put("interval", interval);
		
		storage.write(silentObj.toString(), "silent/" + contextName.replace("/", ".") + "." + channel + "." + sessionId);
	}
	
	private void remove(String id) {
		
		SilentPusherTask pendingSilentTask = silentTaskMap.get(id);
		
		if (pendingSilentTask!=null) {
			
			try {
				pendingSilentTask.cancel();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		silentTaskMap.remove(id);
		
		
	}
	

}
