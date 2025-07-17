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
	
	private Timer timer;
	
	public SilentPusher(PathStorage storage) {
		
		this.storage = storage;
		
		timer = new Timer("Silent Timer");
		
	}
	
	public void register(Session session, String resultOfSilent) {
		
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
		
		/**
		 * For silent task
		 */	
		
		double silentInterval;
		
		try {
			
			silentInterval = Double.parseDouble(session.context().prop("SILENT_INTERVAL"));
		    
		} catch (Exception e4) {
			
			/**
			 * Random between 1-24 Hours
			 */
			Random random = new Random();
			silentInterval = (random.nextInt(25) + 1); 
			
		}
				
		register(silentInterval, contextName, channel, sessionId, false);

	}
	
	public void register(double silentInterval, String contextName, String channel, String sessionId, boolean run) {
		
		SilentPusherTask silentPusherTask = new SilentPusherTask(this, silentInterval, contextName, channel, sessionId);
		
		if (run) {
			
			silentPusherTask.run();
		}
		
		String id = silentPusherTask.id();
		
		//Cancel if there is any pending task.
		cancel(id);
		
		silentTaskMap.put(id, silentPusherTask);
				
	    timer.schedule(silentPusherTask, (int) (silentInterval * 60 * 60 * 1000));
	    
	    /**
	     * Save Task Name
	     */
		System.out.println("Save scheduled task: " + silentPusherTask.id() + " every " + silentInterval + " hours");
		
		JSONObject silentObj = new JSONObject();
		silentObj.put("interval", silentInterval);
		
		storage.write(silentObj.toString(), "silent/" + contextName.replace("/", ".") + "." + channel + "." + sessionId);
	}
	
	private void cancel(String id) {
		
		SilentPusherTask pendingSilentTask = silentTaskMap.get(id);
		
		if (pendingSilentTask!=null) {
			
			try {
				
				System.out.println("Cancel Task.." + id);
				
				pendingSilentTask.cancel();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		silentTaskMap.remove(id);
		
		
	}
	
	public void cancelAll() {
		
		for (String id: silentTaskMap.keySet()) {
			
			cancel(id);
		}
		
		timer.cancel();
		
	}
	

}
