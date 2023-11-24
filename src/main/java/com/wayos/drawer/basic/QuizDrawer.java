package com.wayos.drawer.basic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

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
public class QuizDrawer extends Drawer {

	final String QUESTION_SEPARATOR = "\n\n";
	final String CHOICE_SEPARATOR = "\n-";
	
	private final String content;
	
	private final Set<String> varSet;
	
	private String lastText;
	
	public QuizDrawer(String content) {
		this.content = content;
		this.varSet = new HashSet<>();
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
		
    	String [] questions = content.split(QUESTION_SEPARATOR);
    	
    	Canvas2D.Entity [] parent = new Canvas2D.Entity [] { canvas2D.GREETING };
    	
    	Canvas2D.Entity clearScoreEntity = canvas2D.newEntity(parent, "", "", false);
		canvas2D.nextRow(100);
		canvas2D.nextColumn(200);
    	    	
		parent = new Canvas2D.Entity [] { clearScoreEntity };
    	for (String question:questions) {
    		if (question.trim().isEmpty()) continue;//Skip empty line
    		parent = parse(canvas2D, bundle, question, parent);
    	}
		canvas2D.nextColumn(300);
    	
    	/**
    	 * Update clearSCoreEntity after collected varSet
    	 * `?<varName>=0 ..`
    	 */
		if (!varSet.isEmpty()) {
	    	StringBuilder clearScoreExpressions = new StringBuilder();
	    	for (String var:varSet) {
	    		clearScoreExpressions.append("`?");
	    		clearScoreExpressions.append(var);
	    		clearScoreExpressions.append("=0` ");
	    	}
			
	    	//System.out.println("clearScoreExpressions:" + clearScoreExpressions);
	    	clearScoreEntity.setExpressions(clearScoreExpressions.toString());
	    	clearScoreEntity.reAttachForwarder();//TODO: Reattach forwarder should move into setExpressions?
		}

    	/**
    	 * Log the summary with the last text edited by creator
    	 */    	    	
    	if (lastText!=null) {
    		
    		lastText = lastText.replace("\n", "[br]");
    		
        	String logExpression =  "`?l_quiz=" + lastText + "`";
        	
    		Canvas2D.Entity logEntity = canvas2D.newEntity(parent, "", "", logExpression, false);
    		canvas2D.nextRow(100);
    		canvas2D.nextColumn(200);
    		
    		canvas2D.newEntity(new Canvas2D.Entity[] { logEntity }, "", "", null);
    		canvas2D.nextColumn(200);
    	}
    	
    	//TODO: Should be save in en or not? to avoid invalid choice score calculation base on local lang such as Thai.
    	//canvas2D.context.prop("language", "en");
		
		/**
		 * Create Leaf Entity Forwarder that can forward the lastQuestionId
		 * canvas2D.newEntity(null, "Back", ", #lastQuestionId!", false)
		 */
		
		//appendUserStatsFlow(canvas2D, bundle, scoreForwarderEntity);
    	
	}

	private void appendUserStatsFlow(Canvas2D canvas2D, ResourceBundle bundle, Canvas2D.Entity scoreForwarderEntity) {
		
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
		
		Canvas2D.Entity leaderBoardEntity = canvas2D.newEntity(new Canvas2D.Entity[] { yesEntity, updateNicknameEntity }, "", bundle.getString("quiz.summary") + " #score " + bundle.getString("quiz.points"), null);
	}
	
	private Canvas2D.Entity [] parse(Canvas2D canvas2D, ResourceBundle bundle, String text, Canvas2D.Entity [] parent) {
		
		String [] tokens = text.split(CHOICE_SEPARATOR, 2);
		
		if (tokens.length==2) {
			
			/**
			 * Map to keyword, responseText, choicesText for a question
			 */
			String keyword, responseText, choicesText;		
			keyword = "";
			responseText = tokens[0].trim();
			choicesText = tokens[1];
			
	        return parseQuestion(canvas2D, bundle, keyword, responseText, choicesText, parent);
			
		}
		
		canvas2D.nextColumn(100);
		canvas2D.nextRow(100);
		
		/**
		 * Simple Text
		 * set lastTextEntity to genenerate the log var
		 */
		lastText = text;
		
		return new Canvas2D.Entity[] { canvas2D.newEntity(parent, "", text, false) };

	}

	private Canvas2D.Entity[] parseQuestion(Canvas2D canvas2D, ResourceBundle bundle, String keyword,
			String responseText, String choicesText, Canvas2D.Entity[] parent) {
		
		canvas2D.nextColumn(200);
		canvas2D.nextRow(100);
				
		Canvas2D.Entity knot = canvas2D.newEntity(parent, "", "", false);
		
		canvas2D.nextColumn(200);
		
		Canvas2D.Entity question = canvas2D.newEntity(new Canvas2D.Entity [] { knot }, keyword, responseText, true);
				
		canvas2D.nextColumn(300);
		
		String[] tokens = choicesText.split(CHOICE_SEPARATOR);
        
        List<Canvas2D.Entity> choiceList = new ArrayList<>();
        String exprs;
        String [] t;
        
        for (String c:tokens) {
        	
        	c = c.trim(); //Trim Choice
        	
        	if (c.isEmpty()) continue; //Skip if empty
        	        	
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
            				responseText += " `?lastAnswer=##` `?" + expr + "=+1`";
            				varSet.add(expr);
        				}
        			} else if (!expr.isEmpty()) {
        				responseText += " " + expr;
        			} 
        		}
        		responseText = responseText.trim();
        		
        	} else {
        		
        		keyword = c;
        		
				/**
				 * Record the answer
				 */
        		//responseText = "";
				responseText = " `?lastAnswer=##`";
        		
        	}
        	
    		canvas2D.nextRow(100);
    		
    		keyword = keyword.replace(System.lineSeparator(), " ");
        	    		
    		keyword = keyword.replace("*", "x");

    		//keyword = keyword.replace(" ", "&nbsp;");

    		//keyword = keyword.replace("+", "➕");
    		
    		//keyword = keyword.replace("-", "➖");
    		 		
        	choiceList.add(canvas2D.newEntity(new Canvas2D.Entity [] { question }, keyword, responseText, false));
        	
        }
        
		canvas2D.nextRow(100);
        //Empty choice for alternative answer, skip this question!
    	choiceList.add(canvas2D.newEntity(new Canvas2D.Entity [] { question }, "", "", false));
    	
		//String expr = "`?lastQuestionId=@" + knot.id() + "`";
		String expr = "";
        
		canvas2D.nextColumn(200);
		
		/**
		 * Display the answer
        knot = canvas2D.newEntity(choiceList.toArray(new Canvas2D.Entity[choiceList.size()]), "", bundle.getString("quiz.youranswer") + " #lastAnswer", expr, false);
        */
        
        knot = canvas2D.newEntity(choiceList.toArray(new Canvas2D.Entity[choiceList.size()]), "", "", expr, false);
        
        return new Canvas2D.Entity [] { knot };
    }

}
