package com.wayos.runtime;

import java.util.Scanner;

import com.wayos.Context;
import com.wayos.ContextListener;
import com.wayos.MessageObject;
import com.wayos.NodeEvent;
import com.wayos.Session;
import com.wayos.SessionListener;
import com.wayos.command.wakeup.FlowWakeupCommandNode;
import com.wayos.connector.RequestObject;
import com.wayos.connector.SessionPool;
import com.wayos.connector.SessionPool.ContextFactory;
import com.wayos.context.FileContext;
import com.wayos.context.RemoteContext;

import junit.framework.TestCase;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase
{
	
	public static void main(String [] args) throws Exception {
		
		Context context = new RemoteContext("2361932404058813/1571124198330", "D0ra3m0n", "https://eoss-wayo-bot.appspot.com/s/", "");
		context.load();
		System.out.println(context.prop("title"));
		System.out.println(context.toJSONString());
		
	}
	
    public static void sessionTest() {
    	
    	/**
    	 * Basic Configuration
    	 */
    	
    	SessionPool sessionPool = new com.wayos.connector.SessionPool();
    	
    	sessionPool.register(new ContextFactory() {

			@Override
			public Context createContext(String contextName) {
								
				Context context = new RemoteContext(contextName);
				
				/**
				 * Automatic Backup Your Chatbot Context
				 */
				try {
					context.load();
					FileContext.save("backup/student/" + contextName.replace("/", "-"), context, true);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				return context;
			}

    	});
    	
    	sessionPool.register(new SessionListener() {

			@Override
			public void callback(NodeEvent nodeEvent) {
				
			}

			@Override
			public void onVariablesChanged(Session session) {
				
			}

			@Override
			public void onContextChanged(Session session, Context oldContext, Context newContext) {
				
    		    new FlowWakeupCommandNode(session).execute(null);
				
			}

    	});
    	
    	sessionPool.register(new ContextListener() {

			@Override
			public void callback(NodeEvent nodeEvent) {
				
		        if (nodeEvent.event==NodeEvent.Event.Authentication) {
		        			        	
		            MessageObject messageObject = nodeEvent.messageObject;
		            
		            Context context = (Context) messageObject.attr("context");
		            
		            String token = (String) messageObject.attr("token");

		            /**
		             * Simple Validation
		             */
		            if (token==null || !context.name().equals(token)) {
		                //throw new AdminCommandNode.AuthenticationException("Authentication to AdminCommand");
		            }
		            
		        }

		        if (nodeEvent.event==NodeEvent.Event.ContextSaved) {

		            String contextName = nodeEvent.messageObject.toString();
		            if (contextName.endsWith(".backup")) return;

		        }
		   }
    		
    	});
    	
    	sessionPool.register(new SessionPool.SessionPoolListener() {

			@Override
			public void onNewSession(RequestObject requestObject, Session session) {
				
		        session.vars("#channel", (String) requestObject.messageObject().attr("channel"));
		        session.vars("#targetId", requestObject.sessionId());
			}

			@Override
			public void onRemoveSession(Session session) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTime(Session session) {
				// TODO Auto-generated method stub
				
			}

    	});    	
    	
    	String channel = "console";
    	String sessionId = "" + System.currentTimeMillis();
    	
    	String accountId = "103014451870896";
    	//String botId = "appchain";
    	//String botId = "sscs";
    	String botId = "Yod";
    	
    	String contextName = accountId + "/" + botId;
    	
        Scanner scanner = new Scanner(System.in, "UTF-8");
        
    	String message;
    	
    	while (true) {
    		
    		System.out.print("You>>");
    		
    		message = scanner.nextLine();
    		
    		RequestObject requestObject = createRequestObject(message, channel, sessionId, contextName);
    		Session session = sessionPool.get(requestObject);
    		
    		String responseText = session.parse(requestObject.messageObject());
    		
    		System.out.println("Bot>>" + responseText);
    		
    	}
    	
    }
    
    static RequestObject createRequestObject(String message, String channel, String sessionId, String contextName) {
    	
    	return new RequestObject() {
    		
            @Override
            public MessageObject messageObject() {
                return MessageObject.build(message).attr("channel", channel);
            }

            @Override
            public String sessionId() {
                return sessionId;
            }

            @Override
            public String contextName() {
                return contextName;
            }

        };
    	
    }
    
}
