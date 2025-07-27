package com.wayos.command;

import java.io.File;

import com.wayos.Hook.Match;

import x.org.json.JSONArray;
import x.org.json.JSONObject;

import com.wayos.MessageObject;
import com.wayos.Session;

public class StartProcessCommandNode extends CommandNode {

    public StartProcessCommandNode(Session session, String [] hooks) {
        super(session, hooks, Match.Head);
    }

    @Override
    public String execute(MessageObject messageObject) {
    	
		String params = cleanHooksFrom(messageObject.toString());
		
		String [] arguments = params.split(" ");
		
		try {
			
			ProcessBuilder pb = new ProcessBuilder(arguments);
			
			pb.directory(new File(System.getenv("storagePath")));
			
			pb.inheritIO(); // Optional: redirects output to console
			
			Process process = pb.start();
			
			long pid = process.pid();
						
			JSONObject runningObj = storage().readAsJSONObject("running.json");
			
			if (runningObj!=null) {
				
				JSONArray processIdArray = runningObj.optJSONArray("processIds");
				
				if (processIdArray==null) {
					processIdArray = new JSONArray();
				}
				
				processIdArray.put(pid);
				
				runningObj.put("processIds", processIdArray);
				
				storage().write(runningObj.toString(), "running.json");
				
			}
			
			
	        return "" + pid;
	        
		} catch (Exception e) {
			
			e.printStackTrace();
			
			return e.getMessage();
		}
		
    }
}
