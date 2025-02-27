package com.wayos.command;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.wayos.MessageObject;
import com.wayos.Session;
import com.wayos.Hook.Match;

public class IsExpiredCommandNode extends CommandNode {

	public IsExpiredCommandNode(Session session, String[] hooks) {
		super(session, hooks);
	}
	
	public IsExpiredCommandNode(Session session, String[] hooks, Match match) {
		super(session, hooks, match);
	}

	@Override
	public String execute(MessageObject messageObject) {
		
		String params = cleanHooksFrom(messageObject.toString());
		
		String [] tokens = params.split(" ", 2);
		
		if (tokens.length!=2) {
			System.out.println("BotCallerCommandNode.java (Tokens.length!=2):" + messageObject);
			return "Invalid Parameters <dateFormat> <dateString>";
		}
		
		String pattern = tokens[0];
		String dateString = tokens[1];
		
		try {
			
			SimpleDateFormat sf = new SimpleDateFormat(pattern, session.context().locale());
			Date date = sf.parse(dateString);
			Calendar expiredCalendar = Calendar.getInstance(session.context().locale());
			expiredCalendar.setTime(date);
						
			Calendar nowCalendar = Calendar.getInstance(session.context().locale());
			
			return nowCalendar.after(expiredCalendar) ? "yes" : "no";			
			
		} catch (Exception e) {
			
			e.printStackTrace();
			return e.getMessage();
			
		}
		
	}

}
