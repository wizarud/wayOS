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
 * Content 1
 * - Next
 * 
 * ..
 * 
 * Content N
 * - Next #reach
 * 
 * You got #reach promo code
 * 
 * @author Wisarut Srisawet
 * 
 */
public class PresentationDrawer extends Drawer {

	final String CONTENT_SEPARATOR = "\n\n";
	final String BUTTON_SEPARATOR = "\n-";
	
	private final String content;
	
	private final Set<String> varSet;
	
	private String lastText;
	
	public PresentationDrawer(String content) {
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
		
    	String [] contents = content.split(CONTENT_SEPARATOR);
    	
    	Canvas2D.Entity [] parent = new Canvas2D.Entity [] { canvas2D.GREETING };
    	
    	Canvas2D.Entity clearScoreEntity = canvas2D.newEntity(parent, "", "", false);
		canvas2D.nextRow(100);
		canvas2D.nextColumn(200);
    	    	
		parent = new Canvas2D.Entity [] { clearScoreEntity };
    	for (String paragraph:contents) {
    		if (paragraph.trim().isEmpty()) continue;//Skip empty line
    		parent = parse(canvas2D, bundle, paragraph, parent);
    	}
		canvas2D.nextColumn(300);
    	
    	/**
    	 * Update clearSCoreEntity after collected varList
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
    		
        	String logExpression =  "`?l_presentation=" + lastText + "`";
        	
    		Canvas2D.Entity logEntity = canvas2D.newEntity(parent, "", "", logExpression, false);
    		canvas2D.nextRow(100);
    		canvas2D.nextColumn(200);
    		
    		canvas2D.newEntity(new Canvas2D.Entity[] { logEntity }, "", "", null);
    		canvas2D.nextColumn(200);
    	}
    	
	}
	
	private Canvas2D.Entity [] parse(Canvas2D canvas2D, ResourceBundle bundle, String text, Canvas2D.Entity [] parent) {
		
		String [] tokens = text.split(BUTTON_SEPARATOR, 2);
		
		if (tokens.length==2) {
			
			/**
			 * Map to keyword, responseText, choicesText for a question
			 */
			String keyword, responseText, choicesText;		
			keyword = "";
			responseText = tokens[0].trim();
			choicesText = tokens[1];
			
	        return parseContent(canvas2D, bundle, keyword, responseText, choicesText, parent);
			
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

	private Canvas2D.Entity[] parseContent(Canvas2D canvas2D, ResourceBundle bundle, String keyword,
			String responseText, String buttonsText, Canvas2D.Entity[] parent) {
		
		canvas2D.nextColumn(200);
		canvas2D.nextRow(100);
				
		Canvas2D.Entity knot = canvas2D.newEntity(parent, "", "", false);
		
		canvas2D.nextColumn(200);
		
		Canvas2D.Entity content = canvas2D.newEntity(new Canvas2D.Entity [] { knot }, keyword, responseText, true);
				
		canvas2D.nextColumn(300);
		
		String[] tokens = buttonsText.split(BUTTON_SEPARATOR);
        
        List<Canvas2D.Entity> buttonList = new ArrayList<>();
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
            				responseText += "`?" + expr + "=+1`";
            				varSet.add(expr);
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
    		 		
        	buttonList.add(canvas2D.newEntity(new Canvas2D.Entity [] { content }, keyword, responseText, false));
        	
        }
                
		canvas2D.nextColumn(200);
		
        knot = canvas2D.newEntity(buttonList.toArray(new Canvas2D.Entity[buttonList.size()]), "", "", false);
        
        return new Canvas2D.Entity [] { knot };
    }

}
