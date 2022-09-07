package com.wayos.drawer.basic;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import com.wayos.drawer.Canvas2D;
import com.wayos.drawer.Drawer;

/**
 * Welcome
 * 
 * Blablabla
 * 
 * Question 1
 * - Choice 1.1
 * - Choice 1.2
 * - Choice 1.3
 * - Choice 1.4 #score
 * 
 * Question 2
 * - Choice 1.1
 * - Choice 1.2 #score
 * - Choice 1.3 
 * - Choice 1.4
 *  ..
 * Question(N)
 *  ..
 * You got #score!
 * 
 * @author Wisarut Srisawet
 * 
 */
public class QuestionareDrawer extends Drawer {

	/*
	final String QUESTION_SEPARATOR = "\n\\.\\.\n";
	final String CHOICE_SEPARATOR = "\n\t:";
	final String CHOICE_KEYWORD_RESPONSE_SEPARATOR = "\t";
	*/
	
	final String QUESTION_SEPARATOR = "\n\n";
	final String CHOICE_SEPARATOR = "\n- ";
	//final String CHOICE_KEYWORD_RESPONSE_SEPARATOR = "=>";
	
	private final String content;
	
	public QuestionareDrawer(String content) {
		this.content = content;
	}

	@Override
	public void draw(Canvas2D canvas2D) {
		
    	/**
    	 * Empty Content
    	 */
    	if (content.trim().isEmpty()) return;		
		
    	String [] questions = content.split(QUESTION_SEPARATOR);
    	
    	Canvas2D.Entity [] parent = new Canvas2D.Entity [] { canvas2D.GREETING };
    	
    	Canvas2D.Entity clearScoreEntity = canvas2D.newEntity(parent, "", "", "`?score=`", false);		
		canvas2D.nextRow(100);
		canvas2D.nextColumn(200);
    	    	
		parent = new Canvas2D.Entity [] { clearScoreEntity };
    	for (String question:questions) {
    		parent = parse(canvas2D, question, parent);
    	}
		  
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
    	
		Canvas2D.Entity scoreForwarderEntity = canvas2D.newEntity(parent, "", "", "#score", false);
		canvas2D.nextRow(100);
		canvas2D.nextColumn(200);
		
		/**
		 * No scoring, just thank you and goodbye
		 */
		Canvas2D.Entity thankyouEntity = canvas2D.newEntity(new Canvas2D.Entity[] { scoreForwarderEntity }, "#score", bundle.getString("quiz.thank"), null);
		canvas2D.nextRow(100);
		canvas2D.nextColumn(200);
				
		/**
		 * Has score, Forward #nickname to check if exists
		 */
		Canvas2D.Entity nicknameForwarderEntity = canvas2D.newEntity(new Canvas2D.Entity[] { scoreForwarderEntity }, "", "..(^o^)ๆ", "#nickname", false);
		canvas2D.nextRow(100);
		canvas2D.nextColumn(200);
		
		/**
		 * New Player, Request for Nickname
		 */
		Canvas2D.Entity requestNicknameEntity = canvas2D.newEntity(new Canvas2D.Entity[] { nicknameForwarderEntity }, "#nickname", bundle.getString("quiz.ask.nickname"), true);
		canvas2D.nextRow(100);
		canvas2D.nextColumn(200);
				
		/**
		 * Return Player, Confirm Nickname
		 */
		Canvas2D.Entity confirmNicknameEntity = canvas2D.newEntity(new Canvas2D.Entity[] { nicknameForwarderEntity }, "", bundle.getString("quiz.confirm.nickname") + "\n\n#nickname\n\n" + bundle.getString("quiz.confirm.no"), true);
		canvas2D.nextRow(100);
		canvas2D.nextColumn(200);
		
		Canvas2D.Entity yesEntity = canvas2D.newEntity(new Canvas2D.Entity[] { confirmNicknameEntity }, bundle.getString("quiz.confirm.yes"), "", false);
		canvas2D.nextRow(100);
		canvas2D.nextColumn(200);
		
		/**
		 * Enter & Save nickname
		 */
		Canvas2D.Entity updateNicknameEntity = canvas2D.newEntity(new Canvas2D.Entity[] { requestNicknameEntity, confirmNicknameEntity }, "", bundle.getString("quiz.confirm.nickname") + "\n\n##", "`?nickname=##` `?l_score=#nickname:#score`", false);
		canvas2D.nextRow(100);
		canvas2D.nextColumn(200);
		
		Canvas2D.Entity leaderBoardEntity = canvas2D.newEntity(new Canvas2D.Entity[] { yesEntity, updateNicknameEntity }, "", bundle.getString("quiz.summary") + " #score", null);
    	
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
        String exprs;
        String [] t;
        for (String c:tokens) {
        	
        	if (c.contains("#")) {
        		keyword = c.substring(0, c.indexOf("#")).trim();
        		exprs = c.substring(c.indexOf("#")).trim();
        		t = exprs.split(" ");
        		
        		responseText = "";
        		for (String expr:t) {
        			expr = expr.trim();
        			if (expr.startsWith("#")) {
        				expr = expr.substring(1);
        				if (!expr.isEmpty()) {
            				responseText += " `?" + expr + "=+1`";        					
        				}
        			} else if (!expr.isEmpty()) {
        				responseText += " " + expr;
        			}
        		}
        		responseText = responseText.trim();
        		
        	} else {
        		keyword = c;
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
