package com.wayos.command;

import java.util.HashMap;
import java.util.Map;

import com.wayos.MessageObject;
import com.wayos.command.AsyncCommandNode.FinishListener;

public final class AsyncTask implements Runnable {
	
	public enum Finish {
		
		SUCCESS, 
		ERROR, 
		INTERRUPTED;
		
		private Map<String, Object> attributeMap = new HashMap<>();
		
		public Finish put(String key, Object value) {
			attributeMap.put(key, value);
			return this;
		}
		
		public Object val(String key) {
			return attributeMap.get(key);
		}
	}
	
	public static class Runner {
		
		private AsyncTask asyncTask;
		
		private FinishListener finishListener;
		
		private String generatedOutput;
		
		private boolean active;
		
		public void run (MessageObject messageObject) {}//Do nothing!
		
		public void setFinishListener(FinishListener finishListener, String generatedOutput) {
			
			this.finishListener = finishListener;
			
			this.generatedOutput = generatedOutput;
			
		}
		
		public final void finish(Finish finish) {
						
			/**
			 * Remove from async queue
			 */
			getAsyncCommandNode().session.asyncTaskList().remove(getAsyncTask());
			
			/**
			 * Do next runner if SUCCESS
			 */
			if (finish==Finish.SUCCESS) {
				
				if (finishListener!=null) {
		        	
					finishListener.onFinish(finish.put("generatedOutput", generatedOutput));
				}
				
			}
			
		}

		private void prepare(AsyncTask asyncTask) {
			
			this.asyncTask = asyncTask;
						
			this.active = true;
		}
		
		public AsyncTask getAsyncTask() {
			
			return this.asyncTask;
			
		}
		
		public boolean isActive() {
			
			if (!active) {
				
				System.out.println("Runner Interruped");
				
			}
			
			return active;
		}
		
		public void stop() {
			
			active = false;
		}
		
		public AsyncCommandNode getAsyncCommandNode() {
			
			return asyncTask.asyncCommandNode;
		}
	}
	
	private final AsyncCommandNode asyncCommandNode;
	
	private final MessageObject messageObject;
		
	private final Runner runner;
	
	public AsyncTask (AsyncCommandNode asyncCommandNode, MessageObject messageObject, Runner runner) {
		
		this.asyncCommandNode = asyncCommandNode;
		
		this.messageObject = messageObject;
		
		this.runner = runner;
	}
	
	@Override
	public final void run() {
		
		runner.prepare(this);
				
		runner.run(messageObject);
		
	}
	
	public boolean isActive() {
		
		return runner.isActive();
	}
	
	public void stop() {
		
		runner.stop();
	}
	
	public Runner runner() {
		
		return runner;
	}
		
}	