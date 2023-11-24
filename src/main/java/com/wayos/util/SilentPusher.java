package com.wayos.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Timer;

import com.wayos.Session;
import com.wayos.pusher.PusherUtil;

public class SilentPusher {
	
	Map<String, SilentPusherTask> silentTaskMap = new HashMap<>();
	
	PusherUtil pusherUtil = new PusherUtil();
	
	public SilentPusher() {
		
	}
	
	public void register(Session session) {
		
		/**
		 * For silent task
		 */		
		String silent = session.context().prop("silent");
		
		if (silent==null) return;
		
		silent = silent.trim();
		
		if (silent.isEmpty()) return;
		
		SilentPusherTask silentPusherTask = new SilentPusherTask(this, session);
		
		String id = silentPusherTask.id();
		
		//Cancel if there is any pending task.
		remove(id);
		
		silentTaskMap.put(id, silentPusherTask);
		
		Timer timer = new Timer("Silent Timer");
		
		String silentInterval = session.context().prop("SILENT_INTERVAL");
		
		long interval;
		
		try {
			
			interval = Integer.parseInt(silentInterval) * 60 * 1000; //Every Minutes
			
		} catch (Exception e4) {
			
			Random random = new Random();
			
			interval = (random.nextInt(25) + 1) * 60 * 60 * 1000; //Random between 1-24 Hours
			
		}
		
	    timer.schedule(silentPusherTask, interval);
				
		System.out.println("Scheduled " + silentPusherTask.id() + " every " + (interval / (60 * 1000)) + " minutes");
		
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
