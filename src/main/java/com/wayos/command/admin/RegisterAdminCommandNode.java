package com.wayos.command.admin;

import java.util.List;

import com.wayos.Application;
import com.wayos.Configuration;
import com.wayos.Hook;
import com.wayos.PathStorage;
import com.wayos.MessageObject;
import com.wayos.Session;
import com.wayos.command.CommandNode;
import com.wayos.pusher.PusherUtil;

import x.org.json.JSONObject;

@SuppressWarnings("serial")
public class RegisterAdminCommandNode extends CommandNode {

	public RegisterAdminCommandNode(Session session, String[] hooks) {
		
		super(session, hooks);
	}

	public RegisterAdminCommandNode(Session session, String[] hooks, Hook.Match match) {
		
		super(session, hooks, match);
	}
	
	@Override
	public String execute(MessageObject messageObject) {
		
		PathStorage storage = Application.instance().get(PathStorage.class);
		
		String contextName = session.context().name();
		
		String botName = cleanHooksFrom(messageObject.toString());
		
		if (!botName.isEmpty()) {
			
			/**
			 * TODO: check botName is exists or not?
			 */
			
			String [] tokens = contextName.split("/", 2);
			
			String accountId = tokens[0];
			
			String resourcePath = Configuration.LIB_PATH + accountId + "/";
			
			String botPath = botName + ".context";		
			
			List<String> objectList = storage.listObjectsWithPrefix(resourcePath);
						
			contextName = accountId + "/" + botName;
			
			/**
			 * DEBUG
			 */
			/*
			System.out.println("Register Admin:");
			System.out.println(messageObject.toString());
			System.out.println(botName);
			System.out.println(contextName);
			System.out.println(resourcePath);
			System.out.println(botPath);
			System.out.println(objectList);
			System.out.println("Valid:" + objectList.contains(botPath));						
			System.out.println();
			*/
			
			if (!objectList.contains(botPath)) {
				return botName + " not found!";
			}
			
		}
		
		/**
		 * Only one person and one channel can be admin
		 */
		String channel = session.vars("#channel");
		
		String sessionId = session.vars("#sessionId");
				
		Configuration configuration = new Configuration(contextName);
		
		JSONObject configObject = storage.readAsJSONObject(configuration.adminIdPath());
		
		if (configObject==null) {
			
			configObject = new JSONObject();
			
		} else {
			
			/*
			 * TODO: Alarm the last adminId that would be change to another sessionId
			 */
			
			String lastChannel = configObject.getString("channel");
			
			String lastAdminId = configObject.getString("sessionId");
			
			if (!lastChannel.equals(channel) || !lastAdminId.equals(sessionId)) {
				
				String [] tokens = contextName.split("/");
				
				String accountId = tokens[0];
				
				String botId = tokens[1];
				
				PusherUtil pusherUtil = Application.instance().get(PusherUtil.class);
				
				pusherUtil.push(accountId, botId, lastChannel, lastAdminId, "..(-.-)");
				
			}
			
		}

		configObject.put("channel", channel);
		
		configObject.put("sessionId", sessionId);
	
		storage.write(configObject.toString(), configuration.adminIdPath());
		
		messageObject.attr("selfSign", Configuration.brainySecret);
		
		return "(^o^)ๆ";
	}

}
