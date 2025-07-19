package com.wayos.command;

import java.time.ZonedDateTime;

import com.wayos.Application;
import com.wayos.MessageObject;
import com.wayos.PathStorage;
import com.wayos.Session;

import x.org.json.JSONObject;

import com.wayos.Hook.Match;
import com.wayos.util.SilentPusher;
import com.wayos.util.SilentPusherTask;

public class CronUpdateCommandNode extends CommandNode {

	public CronUpdateCommandNode(Session session, String[] hooks) {
		super(session, hooks);
	}
	
	public CronUpdateCommandNode(Session session, String[] hooks, Match match) {
		super(session, hooks, match);
	}

	@Override
	public String execute(MessageObject messageObject) {
		
		String params = cleanHooksFrom(messageObject.toString());
				
		String [] tokens = params.split(" ", 2);
		
		if (tokens.length!=2) {
			
			System.out.println("CronUpdateCommandNode: Invalid parameters! " + messageObject);
			
			return "Invalid Parameters Keyword, Cron Expression";
			
		}
		
		String messageToFire = tokens[0];
		
		String cronExpression = tokens[1];
				
		PathStorage storage = Application.instance().get(PathStorage.class);
		
		String contextName = session.context().name();		
		String channel = session.vars("#channel");		
		String sessionId = session.vars("#sessionId");
				
		SilentPusherTask silentPusherTask = new SilentPusherTask(cronExpression, contextName, channel, sessionId, messageToFire);
		
		SilentPusher silentPusher = Application.instance().get(SilentPusher.class);
				
		/**
		 * Delete Cron
		 */
		if (cronExpression.equals("delete")) {
			
			String taskId = silentPusherTask.id();
									
			silentPusher.cancel(taskId);
			
			System.out.println("Deleteing Cron.." + taskId);
			
			storage.delete("silent/" + taskId);
			
			return taskId + " deleted";
		}
		
		ZonedDateTime next = silentPusher.register(silentPusherTask);
		
		JSONObject cronObj = new JSONObject();
		cronObj.put("interval", cronExpression);
		
		storage.write(cronObj.toString(), "silent/" + silentPusherTask.id());

		return next.toString();
			
	}

}
