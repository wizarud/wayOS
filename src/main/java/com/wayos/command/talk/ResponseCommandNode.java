package com.wayos.command.talk;

import com.wayos.MessageObject;
import com.wayos.Session;
import com.wayos.command.AsyncCommandNode;
import com.wayos.command.AsyncTask;
import com.wayos.expression.Expression;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResponseCommandNode extends ProblemCommandNode implements AsyncCommandNode.FinishListener {

    public final String responseText;

    public ResponseCommandNode(Session session, String responseText) {
        super(session);
        this.responseText = responseText;
    }

    public String _execute(MessageObject messageObject) {

        Pattern pattern = Pattern.compile("\\`(.|\\n|\\r|\\t)*?\\`", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(responseText);

        String evaluatedText = responseText;
        String expression;
        while (matcher.find()) {
            expression = matcher.group();
            evaluatedText = evaluatedText.replace(expression, Expression.build(session, expression).execute(messageObject));
        }
        
        return session.parameterized(messageObject, evaluatedText);
    }

    @Override
    public String execute(MessageObject messageObject) {

        Pattern pattern = Pattern.compile("\\`(.|\\n|\\r|\\t)*?\\`", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(responseText);

        String evaluatedText = responseText;
        String expression;
        while (matcher.find()) {
            expression = matcher.group();
            evaluatedText = evaluatedText.replace(expression, Expression.build(session, expression).execute(messageObject));
        }
        
        /**
         * All Sync Command Finish, Do Async after that..
         */
        
        String generatedOutput = session.parameterized(messageObject, evaluatedText);        
                    	    	
        if (session.asyncTaskList().size() > 0) {
        	        	
        	System.out.println("Start AsyncRunners in queue");
        	
        	class AsyncCommandThread extends Thread {
        		
        		AsyncCommandThread() {
        			
                	for (AsyncTask asyncCommand:session.asyncTaskList()) {
                		
                		asyncCommand.runner().setFinishListener(ResponseCommandNode.this, generatedOutput);
                		
                	}
        		}
            	
        		public void run() {
        			                	                	
        			new Thread() {
        				
        				public void run() {
        					
        					/**
        					 * Run Next AsyncCommand on onFinish
        					 */
        					session.asyncTaskList().get(0).run();        					
        					
        				}
        			}.start();
        			
        			        			
        		}
        		
        	};
        	
        	new AsyncCommandThread().start();
        	
        }        
        
        return generatedOutput;
    }
    
	@Override
	public void onFinish(AsyncTask.Finish finish) {

		if (session.asyncTaskList().size() > 0) {
			
			new Thread() {
				
				public void run() {
					
					/**
					 * Run Next AsyncCommand on onFinish
					 */
					session.asyncTaskList().get(0).run();        					
					
				}
			}.start();
			
		}
		
	}
    
}
