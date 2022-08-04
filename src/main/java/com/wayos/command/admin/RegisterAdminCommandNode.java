package com.wayos.command.admin;

import org.json.JSONObject;

import com.wayos.Configuration;
import com.wayos.PathStorage;
import com.wayos.MessageObject;
import com.wayos.Session;
import com.wayos.command.CommandNode;
import com.wayos.util.Application;

@SuppressWarnings("serial")
public class RegisterAdminCommandNode extends CommandNode {

	public RegisterAdminCommandNode(Session session, String[] hooks) {
		
		super(session, hooks);
	}

	@Override
	public String execute(MessageObject messageObject) {
		
		/**
		 * Only one person and one channel can be admin
		 */
		String channel = session.vars("#channel");
		
		String sessionId = session.vars("#sessionId");
		
		PathStorage storage = Application.instance().get(PathStorage.class);
		
		Configuration configuration = new Configuration(session.context().name());
		
		JSONObject configObject = storage.readAsJSONObject(configuration.adminIdPath());
		
		if (configObject==null) {
			
			configObject = new JSONObject();
		}

		configObject.put("channel", channel);
		
		configObject.put("sessionId", sessionId);
	
		storage.write(configObject.toString(), configuration.adminIdPath());
		
		messageObject.attr("selfSign", Configuration.brainySecret);
		
		return "(^o^)ๆ";
	}

}
