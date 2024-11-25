package com.wayos.expression;

import com.wayos.MessageObject;
import com.wayos.Session;
import com.wayos.command.CommandNode;

public class CommandExpression extends Expression {

	public CommandExpression(Session session, String[] arguments) {
		super(session, arguments);
	}
	
    @Override
    public String execute(MessageObject messageObject) {

        String [] args = parameterized(messageObject, arguments);
        
        if (args.length<=3) {
        	
            String params = "";
            
            if (args.length==3) {
            	params = args[2];        	
            }
            
        	String hook = args[1];
        	
        	MessageObject hookMessageObject = MessageObject.build(hook);
        	MessageObject paramsMessageObject = MessageObject.build(params);
        	
        	//System.out.println(hookMessageObject);
        	//System.out.println(paramsMessageObject);
        	
            String result = "";
            
            session.clearProblem();// Clear Question incase of calling command under choice
            
    		for (CommandNode node : session.commandList()) {
    			
    			if (node.matched(hookMessageObject)) {
    				
    				//System.out.println("matched: " + node.getClass().getName());
    				
    				result = node.execute(paramsMessageObject);
    				
    				break;
    				
    			}
    		}
            
            messageObject.addResult(result);
            
            return "";        	
        }
        
        return super.execute(messageObject);
    }

}
