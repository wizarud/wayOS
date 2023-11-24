package com.wayos.drawer.basic;

import java.util.Locale;
import java.util.ResourceBundle;

import com.wayos.drawer.Canvas2D;
import com.wayos.drawer.Drawer;

/**
 * 
 * Greeting(N)
 * 
 * Question 1?
 * Answer 1
 * 
 * Question 2?
 * Answer 2
 *  ..
 * Question(N)
 *  ..
 *  
 * Unknown(N)
 * 
 * @author Wisarut Srisawet
 * 
 */
public class FAQDrawer extends Drawer {

	final String QUESTION_SEPARATOR = "\n\n";
	final String ANSWER_SEPARATOR = "\\?";
	
	private final String content;
	
	private boolean isUnknown;
	
	public FAQDrawer(String content) {
		this.content = content;
	}

	@Override
	public void draw(Canvas2D canvas2D) {
		
    	/**
    	 * Empty Content
    	 */
    	if (content.trim().isEmpty()) return;		
    	
    	/**
    	 * Append Logic Flow if #score occurs
		 * Forward #score to check if exists
    	 */
		String language = canvas2D.context.prop("language");
		if (language==null) {
			language = "en";
		}
		
		Locale locale = new Locale(language);
		
		ResourceBundle bundle = ResourceBundle.getBundle("com.wayos.i18n.text", locale);		
		
    	Canvas2D.Entity greetingParent = canvas2D.GREETING;
    	
    	Canvas2D.Entity unknownParent = canvas2D.UNKNOWN;
    	
    	String [] questions = content.split(QUESTION_SEPARATOR);
    	
    	String keywords, responseText;
    	
    	for (String text:questions) {
    		
    		if (text.trim().isEmpty()) continue;//Skip empty line
    		
    		String [] tokens = text.split(ANSWER_SEPARATOR, 2);
    		
    		if (tokens.length==2 && !tokens[1].trim().isEmpty()) {
    			
    			/**
    			 * Map to keywords, responseText for a FAQ
    			 */
    			keywords = tokens[0].trim();
    			responseText = tokens[1].trim();
    			
    	        canvas2D.newEntity(null, keywords, responseText, null);
    			
    			canvas2D.nextRow(200);
    			
    			isUnknown = true; //Set to unknown state after a question found!
    			
    			continue;
    		}
    		
    		responseText = tokens[0].trim();
    		
    		if (!isUnknown) {
    			
    			greetingParent = canvas2D.newEntity(new Canvas2D.Entity [] { greetingParent }, "", responseText, false);
    			
    		} else {
    			
    			unknownParent = canvas2D.newEntity(new Canvas2D.Entity [] { unknownParent }, "", responseText, "##", false);
    			
    		}
    		
			canvas2D.nextColumn(200);    			
			canvas2D.nextRow(100);
    	}
    	
    	greetingParent.setQuestion(null);
    	
		//Tail of unknown, Log it!
		Canvas2D.Entity logUnknownEntity = canvas2D.newEntity(new Canvas2D.Entity[] { unknownParent }, "", "", "`?l_unknown=##`", false);
		
		canvas2D.nextRow(100);
		canvas2D.nextColumn(200);
		
		canvas2D.newEntity(new Canvas2D.Entity[] { logUnknownEntity }, "", "", null);
		
	}
	
}
