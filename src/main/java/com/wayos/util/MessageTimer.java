package com.wayos.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
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

public class MessageTimer {
	
	private Map<String, MessageTimerTask> messageTimerTaskMap = new HashMap<>();
	
	private Timer timer;
	
	public MessageTimer(PathStorage storage) {
		
		timer = new Timer("Message Timer");
		
	}
	
	public void register(MessageTimerTask messageTimerTask) {
				
		String id = messageTimerTask.id();
		
		//Cancel if there is any pending task.
		cancel(id);
		
		messageTimerTaskMap.put(id, messageTimerTask);
				
		try {

			/**
			 * Parse Interval for interger in hours
			 */
			
			double hours = Double.parseDouble(messageTimerTask.timeExpression());
			
			long delay = (long) (hours * 60 * 60 * 1000);

		    timer.schedule(messageTimerTask, delay);
		    
	        Instant now = Instant.now();
	        Instant future = now.plusMillis(delay);
	        
	        ZonedDateTime next = future.atZone(ZoneId.systemDefault());
	        
		    messageTimerTask.setNextExecute(next);

			messageTimerTask.repeatIfFinishBy(this);
			
		} catch (Exception e) {
			
			//e.printStackTrace();
			
			try {
				
				/**
				 * Parse Current Date at HH:mm, Do only one time
				 */
				
		        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
		        
		        LocalTime time = LocalTime.parse(messageTimerTask.timeExpression(), timeFormatter);

		        LocalDateTime todayWithTime = LocalDate.now().atTime(time);

		        Date date = Date.from(todayWithTime.atZone(ZoneId.systemDefault()).toInstant());				
				
				timer.schedule(messageTimerTask, date);
				
				ZonedDateTime next = date.toInstant().atZone(ZoneId.systemDefault());
				
			    messageTimerTask.setNextExecute(next);// Just information
			    
				messageTimerTask.repeatIfFinishBy(null); //null means no repeat!!!
				
			} catch (Exception HHmmFormatException) {
				
				try {
					
					/**
					 * Parse Cron format for long running task
					 */
					
			        ZonedDateTime now = ZonedDateTime.now();
					
					CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
			        Cron cron = parser.parse(messageTimerTask.timeExpression());
			        cron.validate();
			        		        
			        ExecutionTime executionTime = ExecutionTime.forCron(cron);
			        
			        ZonedDateTime next = executionTime.nextExecution(now).get();
			        
		            long delay = java.time.Duration.between(now, next).toMillis();
		            
				    timer.schedule(messageTimerTask, delay);
				    
				    next = next.toInstant().atZone(ZoneId.systemDefault());
				    
				    messageTimerTask.setNextExecute(next);
				    
					messageTimerTask.repeatIfFinishBy(this);
					
				} catch (Exception cronExpressionException) {
					
					cronExpressionException.printStackTrace();
					
				}				
				
			}
			
			
		} finally {
						
			/**
			 * Save for reexecute after restart and show status in dashboard
			 */
			
			PathStorage storage = Application.instance().get(PathStorage.class);
			
			storage.write(messageTimerTask.toJSONObject().toString(), "silent/" + messageTimerTask.id() + ".json");

		}

	}
	
	public void cancel(String id) {
		
		MessageTimerTask pendingMessageTimerTask = messageTimerTaskMap.get(id);
		
		if (pendingMessageTimerTask!=null) {
			
			try {
				
				System.out.println("Cancel Task.." + id);
				
				pendingMessageTimerTask.stop();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		messageTimerTaskMap.remove(id);
				
	}
	
	public void cancelAll() {
		
		for (String id: messageTimerTaskMap.keySet()) {
			
			cancel(id);
		}
		
		timer.cancel();
		
	}

}
