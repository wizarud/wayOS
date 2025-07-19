package com.wayos.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import com.wayos.PathStorage;

public class SilentPusher {
	
	private Map<String, SilentPusherTask> silentTaskMap = new HashMap<>();
	
	private Timer timer;
	
	public SilentPusher(PathStorage storage) {
		
		timer = new Timer("Silent Timer");
		
	}
	
	public ZonedDateTime register(SilentPusherTask silentPusherTask) {
				
		String id = silentPusherTask.id();
		
		//Cancel if there is any pending task.
		cancel(id);
		
		silentTaskMap.put(id, silentPusherTask);
		
		silentPusherTask.repeatIfFinishBy(this);
		
		try {

			double hours = Double.parseDouble(silentPusherTask.cronExpression());
			
			long delay = (long) (hours * 60 * 60 * 1000);

		    timer.schedule(silentPusherTask, delay);
		    
	        Instant now = Instant.now();
	        Instant future = now.plusMillis(delay);
	        
	        return future.atZone(ZoneId.systemDefault());

		} catch (Exception e) {
			
			e.printStackTrace();
			
			try {
				
		        ZonedDateTime now = ZonedDateTime.now();
				
				CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
		        Cron cron = parser.parse(silentPusherTask.cronExpression());
		        cron.validate();
		        		        
		        ExecutionTime executionTime = ExecutionTime.forCron(cron);
		        
		        ZonedDateTime next = executionTime.nextExecution(now).get();
		        
	            long delay = java.time.Duration.between(now, next).toMillis();
	            
			    timer.schedule(silentPusherTask, delay);
			    
			    return next.toInstant().atZone(ZoneId.systemDefault());
			    
			} catch (Exception cronExpressionException) {
				
				cronExpressionException.printStackTrace();
				
			}
			
		}		
				
	    return null;
	}
	
	public void cancel(String id) {
		
		SilentPusherTask pendingSilentTask = silentTaskMap.get(id);
		
		if (pendingSilentTask!=null) {
			
			try {
				
				System.out.println("Cancel Task.." + id);
				
				pendingSilentTask.stop();
				
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
