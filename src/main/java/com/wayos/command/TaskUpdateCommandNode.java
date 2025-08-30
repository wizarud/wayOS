package com.wayos.command;

import com.wayos.Application;
import com.wayos.MessageObject;
import com.wayos.PathStorage;
import com.wayos.Session;

import com.wayos.util.MessageTimer;
import com.wayos.util.MessageTimerTask;

public class TaskUpdateCommandNode extends CommandNode {

	public TaskUpdateCommandNode(Session session, String[] hooks) {
		super(session, hooks);
	}
	
	@Override
	public String execute(MessageObject messageObject) {
		
		String params = cleanHooksFrom(messageObject.toString());
				
		String [] tokens = params.split(" ", 2);
		
		if (tokens.length!=2) {
			
			System.out.println("TaskUpdateCommandNode: Invalid parameters! " + messageObject);
			
			return "Invalid Parameters <Time Expression> <Keywords> to fire";
			
		}
		
		String timeExpression = tokens[0];
		
		String messageToFire = tokens[1];

		PathStorage storage = Application.instance().get(PathStorage.class);
		
		String contextName = session.context().name();
		
		String sessionId = session.vars("#sessionId");

		MessageTimerTask silentFireTask = new MessageTimerTask(timeExpression, contextName, sessionId, messageToFire);
		
		MessageTimer silentFire = Application.instance().get(MessageTimer.class);

		/**
		 * Delete Task
		 */
		if (timeExpression.equals("delete")) {
			
			String taskId = silentFireTask.id();

			silentFire.cancel(taskId);
			
			System.out.println("Deleteing Task.." + taskId);
			
			storage.delete("silent/" + taskId + ".json");
			
			return taskId + " deleted";
		}
		
		silentFire.register(silentFireTask);
		
		return silentFireTask.nextExecute().toString();
			
	}

}
