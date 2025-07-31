package com.wayos.command.wakeup;

import com.wayos.Hook.Match;
import com.wayos.MessageObject;
import com.wayos.Session;
import com.wayos.command.BotCallerCommandNode;
import com.wayos.command.CommandNode;
import com.wayos.command.CronUpdateCommandNode;
import com.wayos.command.GreetingCommandNode;
import com.wayos.command.IsExpiredCommandNode;
import com.wayos.command.Key;
import com.wayos.command.StartProcessCommandNode;
import com.wayos.command.StopProcessCommandNode;
import com.wayos.command.WaitCommandNode;
import com.wayos.command.WakeCommandNode;
import com.wayos.command.admin.AdminCommandNode;
import com.wayos.command.admin.RegisterAdminCommandNode;
import com.wayos.command.data.*;
import com.wayos.command.talk.FlowTalkCommandNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ExtensionSupportWakeupCommandNode extends CommandNode {
	
	public interface NewSessionListener {
		public void wakup(Session session);
	}
	
	public static abstract class WebListener implements ServletContextListener, NewSessionListener {

		@Override
		public void contextInitialized(ServletContextEvent sce) {
					
			List<ExtensionSupportWakeupCommandNode.NewSessionListener> newSessionListenerList = 
					(List<ExtensionSupportWakeupCommandNode.NewSessionListener>) 
					sce.getServletContext().getAttribute("ExtensionSupportWakeupCommandNode.NewSessionListener");
			
			if (newSessionListenerList==null) {
				
				newSessionListenerList = new ArrayList<>();
				
			}
			
			newSessionListenerList.add(this);
			sce.getServletContext().setAttribute("ExtensionSupportWakeupCommandNode.NewSessionListener", newSessionListenerList);
			
		}

		@Override
		public void contextDestroyed(ServletContextEvent sce) {
		}

		@Override
		public abstract void wakup(Session session);

	}	

    public static final Key KEY = new Key("\uD83D\uDE0A", "?", Arrays.asList("\uD83D\uDC4D", "\uD83D\uDC4E", "ไม่"));
    
    private List<NewSessionListener> newSessionListenerList;
    
    public ExtensionSupportWakeupCommandNode(Session session) {
    	
        super (session);
        
        newSessionListenerList = new ArrayList<>();
    }
    
    public void addNewSessionListener(List<NewSessionListener> newSessionListenerList) {
    	
    	if (newSessionListenerList!=null) {
    		
    		this.newSessionListenerList.addAll(newSessionListenerList);
    		
    	}
    }

    @Override
    public String execute(MessageObject messageObject) {

        /**
         * Protected from bad words
         */
        session.protectedList().clear();

        /**
         * Command list is Ordered by Priority
         */
        session.adminCommandList().clear();
         
        session.adminCommandList().add(new AdminCommandNode(new ImportWayDataCommandNode(session, new String[]{"wayos"})));

        session.commandList().clear();
        session.commandList().add(new RegisterAdminCommandNode(session, new String[]{"ลงทะเบียนผู้ดูแล"}));
        session.commandList().add(new GreetingCommandNode(session, new String[]{"greeting", "ดีจ้า"}));
        session.commandList().add(new WakeCommandNode(session, new String[]{"silent"}));
        
        /**
         * Add Extension Commands!
         */
        for (NewSessionListener newSessionListener:newSessionListenerList) {
        	newSessionListener.wakup(session);
        }
        
        session.commandList().add(new StartProcessCommandNode(session, new String[]{"start"}));
        session.commandList().add(new WaitCommandNode(session, new String[]{"wait"}));
        session.commandList().add(new StopProcessCommandNode(session, new String[]{"stop"}));
        
        session.commandList().add(new BotCallerCommandNode(session, new String[]{"call"}, Match.Head));
        //session.commandList().add(new DateStringToTimestampCommandNode(session, new String[]{"dateStringToTimestamp"}, Match.Head));
        session.commandList().add(new IsExpiredCommandNode(session, new String[]{"expired"}, Match.Head));
        
        session.commandList().add(new CronUpdateCommandNode(session, new String[]{"crons"}, Match.Head));
                
        session.commandList().add(new FlowTalkCommandNode(session, KEY));

        return "\\(^o^)ๆ";
    }
}
