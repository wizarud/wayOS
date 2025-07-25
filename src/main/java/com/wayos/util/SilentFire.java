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
import com.wayos.Application;
import com.wayos.PathStorage;

public class SilentFire {
	
	private Map<String, SilentFireTask> silentTaskMap = new HashMap<>();
	
	private Timer timer;
	
	public SilentFire(PathStorage storage) {
		
		timer = new Timer("Silent Timer");
		
	}
	
	public void register(SilentFireTask silentFireTask) {
				
		String id = silentFireTask.id();
		
		//Cancel if there is any pending task.
		cancel(id);
		
		silentTaskMap.put(id, silentFireTask);
		
		silentFireTask.repeatIfFinishBy(this);
		
		try {

			double hours = Double.parseDouble(silentFireTask.cronExpression());
			
			long delay = (long) (hours * 60 * 60 * 1000);

		    timer.schedule(silentFireTask, delay);
		    
	        Instant now = Instant.now();
	        Instant future = now.plusMillis(delay);
	        
	        ZonedDateTime next = future.atZone(ZoneId.systemDefault());
	        
		    silentFireTask.setNextExecute(next);

		} catch (Exception e) {
			
			//e.printStackTrace();
			
			try {
				
		        ZonedDateTime now = ZonedDateTime.now();
				
				CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
		        Cron cron = parser.parse(silentFireTask.cronExpression());
		        cron.validate();
		        		        
		        ExecutionTime executionTime = ExecutionTime.forCron(cron);
		        
		        ZonedDateTime next = executionTime.nextExecution(now).get();
		        
	            long delay = java.time.Duration.between(now, next).toMillis();
	            
			    timer.schedule(silentFireTask, delay);
			    
			    next = next.toInstant().atZone(ZoneId.systemDefault());
			    
			    silentFireTask.setNextExecute(next);
			    
			} catch (Exception cronExpressionException) {
				
				cronExpressionException.printStackTrace();
				
			}
			
		} finally {
						
			/**
			 * Save for reexecute after restart and show status in dashboard
			 */
			
			PathStorage storage = Application.instance().get(PathStorage.class);
			
			storage.write(silentFireTask.toJSONObject().toString(), "silent/" + silentFireTask.id() + ".json");

		}

	}
	
	public void cancel(String id) {
		
		SilentFireTask pendingSilentTask = silentTaskMap.get(id);
		
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
