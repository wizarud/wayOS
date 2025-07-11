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
 * #fullName
 * 
 * Question 2
 * #address
 *  ..
 * Question(N)
 *  ..
 * 
 * @author Wisarut Srisawet
 * 
 */
public class FormDrawer extends Drawer {

	final String QUESTION_SEPARATOR = "\n\n";
	final String VAR_SEPARATOR = "\n#";
	
	private final String content;
	
	private final Set<String> varSet;

	private String lastText;
	
	public FormDrawer(String content) {
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
    	
    	Canvas2D.Entity clearVarsEntity = canvas2D.newEntity(parent, "", "", false);
		canvas2D.nextRow(100);
		canvas2D.nextColumn(200);
    	    	
		parent = new Canvas2D.Entity [] { clearVarsEntity };
    	
    	for (String question:questions) {
    		if (question.trim().isEmpty()) continue;//Skip empty line
    		parent = parse(canvas2D, bundle, question, parent);
    	}
		canvas2D.nextColumn(300);
		
    	/**
    	 * Update clearVarsEntity after collected varSet
    	 * `?<varName>= ..`
    	 */
		if (!varSet.isEmpty()) {
	    	StringBuilder clearVarsExpressions = new StringBuilder();
	    	for (String var:varSet) {
	    		clearVarsExpressions.append("`?");
	    		clearVarsExpressions.append(var);
	    		clearVarsExpressions.append("=` ");
	    	}
			
	    	//System.out.println("clearScoreExpressions:" + clearScoreExpressions);
	    	clearVarsEntity.setExpressions(clearVarsExpressions.toString());
	    	clearVarsEntity.reAttachForwarder();//TODO: Reattach forwarder should move into setExpressions?
		}
		
		
    	/**
    	 * Log the summary with the last text edited by creator
    	 */    	    	
    	if (lastText!=null) {

    		//lastText = lastText.replace("\n", "[br]");
    		
        	String logExpression =  "`?l_quiz=" + lastText + "`";
        	
    		Canvas2D.Entity logEntity = canvas2D.newEntity(parent, "", "", logExpression, false);
    		canvas2D.nextRow(100);
    		canvas2D.nextColumn(200);
    		
    		canvas2D.newEntity(new Canvas2D.Entity[] { logEntity }, "", "", null);		
    		canvas2D.nextColumn(200);
    	}
		
	}
	
	private Canvas2D.Entity [] parse(Canvas2D canvas2D, ResourceBundle bundle, String text, Canvas2D.Entity [] parent) {
		
		String [] tokens = text.split(VAR_SEPARATOR, 2);
		
		if (tokens.length==2 && !tokens[1].trim().isEmpty()) {
			
			/**
			 * Map to responseText, varName for a question
			 */
			String responseText = tokens[0].trim();
			String varText = tokens[1].trim();
			
	        return parseQuestion(canvas2D, bundle, responseText, varText, parent);
			
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

	private Canvas2D.Entity[] parseQuestion(Canvas2D canvas2D, ResourceBundle bundle, String responseText, String inputText, Canvas2D.Entity[] parent) {
		
		String varName = inputText.trim();
		
		/**
		 * If this entity is a question so its never be empty for varName!		
		if (varName.isEmpty()) {
			varName = "next";
		}
		 */
		
		varSet.add(varName);		
		
		canvas2D.nextColumn(200);
		canvas2D.nextRow(100);
				
		Canvas2D.Entity knot = canvas2D.newEntity(parent, "", "", false);
		
		canvas2D.nextColumn(200);
		
		Canvas2D.Entity question = canvas2D.newEntity(new Canvas2D.Entity [] { knot }, "", responseText, true);
				
		canvas2D.nextColumn(300);
		
        List<Canvas2D.Entity> choiceList = new ArrayList<>();
        
		/**
		 * Review Button
		 * Create Leaf Entity Forwarder that can forward to this question
		 * canvas2D.newEntity(null, "Back", ", #<varName>!", false)
		 */
        //canvas2D.newEntity(new Canvas2D.Entity [] { question }, bundle.getString("form.review"), ", @" + knot.id() + "!", false);
		//canvas2D.nextRow(100);
		
		/**
		 * Next Button
		 */
    	choiceList.add(canvas2D.newEntity(new Canvas2D.Entity [] { question }, bundle.getString("form.next"), "", false));
		canvas2D.nextRow(100);
    	
		/**
		 * Input Answer Entity
		 */
		choiceList.add(canvas2D.newEntity(new Canvas2D.Entity [] { question }, "", " `?" + varName + "=##`", false));
		canvas2D.nextColumn(200);
		canvas2D.nextRow(100);
		
		/*
		 * Redisplay the answer if occurs!
		 * 
		Canvas2D.Entity varForwarderEntity = canvas2D.newEntity(choiceList.toArray(new Canvas2D.Entity[choiceList.size()]), "", "", "#" + varName, false);
		canvas2D.nextColumn(200);
		canvas2D.nextRow(100);
		
		Canvas2D.Entity skipVarEntity = canvas2D.newEntity(new Canvas2D.Entity [] { varForwarderEntity }, "#" + varName, "", false);
		canvas2D.nextRow(100);
        
		Canvas2D.Entity editedVarEntity = canvas2D.newEntity(new Canvas2D.Entity [] { varForwarderEntity }, "", bundle.getString("quiz.youranswer") + " " + "#" + varName, false);
		canvas2D.nextColumn(200);
		canvas2D.nextRow(100);
		
		knot = canvas2D.newEntity(new Canvas2D.Entity [] { skipVarEntity, editedVarEntity }, "", "", false);
		*/
		
		knot = canvas2D.newEntity(choiceList.toArray(new Canvas2D.Entity[choiceList.size()]), "", "", false);
        
        return new Canvas2D.Entity [] { knot };
        
    }

}
