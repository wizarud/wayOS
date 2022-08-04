package com.wayos.drawer.basic;

import java.util.ArrayList;
import java.util.List;

import com.wayos.drawer.Canvas2D;
import com.wayos.drawer.Drawer;

/**
 * Create Sequencial of Questionare
 * Support Multiline of question label
 * eertert
 * erterter
 * ertertret
 * 		:Choice1	Response1
 * 		:Choice2 Response2 `?score=+1`
 * 		:Choice3 Response3
 * 	.. (Seperator)
 * eertert
 * erterter
 * ertertret
 * 		:Choice1 Response1
 * 		:Choice2 Response2 
 * 		:Choice3 Response3 `?score=+1`
 *  ..
 * 	Question(N)
 * 	You got #score!
 */

public class QuestionareDrawer extends Drawer {

	/*
	final String QUESTION_SEPARATOR = "\n\\.\\.\n";
	final String CHOICE_SEPARATOR = "\n\t:";
	final String CHOICE_KEYWORD_RESPONSE_SEPARATOR = "\t";
	*/
	
	final String QUESTION_SEPARATOR = "\n\n";
	final String CHOICE_SEPARATOR = "\n- ";
	final String CHOICE_KEYWORD_RESPONSE_SEPARATOR = "=>";
	
	private final String content;
	
	public QuestionareDrawer(String content) {
		this.content = content;
	}

	@Override
	public void draw(Canvas2D canvas2D) {
		
    	String [] questions = content.split(QUESTION_SEPARATOR);
    	
    	Canvas2D.Entity [] parent = new Canvas2D.Entity [] { canvas2D.GREETING };
    	for (String question:questions) {
    		parent = parse(canvas2D, question, parent);
    	}		
		
	}
	
	private Canvas2D.Entity [] parse(Canvas2D canvas2D, String text, Canvas2D.Entity [] parent) {
		
		String [] tokens = text.split(CHOICE_SEPARATOR, 2);
		
		if (tokens.length==2) {
			
			/**
			 * Map to keyword, responseText, choicesText for a question
			 */
			String keyword, responseText, choicesText;		
			keyword = "";
			responseText = tokens[0].trim();
			choicesText = tokens[1];
			
	        return parseQuestion(canvas2D, keyword, responseText, choicesText, parent);
			
		}
		
		canvas2D.nextColumn(100);
		canvas2D.nextRow(100);
		
		/**
		 * Simple Text
		 */
		return new Canvas2D.Entity[] { canvas2D.newEntity(parent, "", text, false) };
	}

	private Canvas2D.Entity[] parseQuestion(Canvas2D canvas2D, String keyword,
			String responseText, String choicesText, Canvas2D.Entity[] parent) {
		
		canvas2D.nextColumn(100);
		canvas2D.nextRow(100);
		
		String[] tokens;
		Canvas2D.Entity question = canvas2D.newEntity(parent, keyword, responseText, true);
        
        tokens = choicesText.split(CHOICE_SEPARATOR);
        
        List<Canvas2D.Entity> choiceList = new ArrayList<>();
        String [] t;
        for (String c:tokens) {
        	
        	t = c.split(CHOICE_KEYWORD_RESPONSE_SEPARATOR, 2);
        	
        	if (t.length==2) {
            	keyword = t[0];
            	responseText = t[1];        		
        	} else if (t.length==1) {
        		keyword = t[0];
        		responseText = "";
        	}
        	
    		canvas2D.nextRow(100);
    		
    		keyword = keyword.replace(" ", "&nbsp;");

    		keyword = keyword.replace("+", "➕");
    		
    		keyword = keyword.replace("-", "➖");
    		
    		keyword = keyword.replace("*", "✖️");
    		
    		keyword = keyword.replace(",", "&#44;");
 		
        	choiceList.add(canvas2D.newEntity(new Canvas2D.Entity [] { question }, keyword, responseText, false));
        }
        
		return choiceList.toArray(new Canvas2D.Entity[choiceList.size()]);
	}	

}
