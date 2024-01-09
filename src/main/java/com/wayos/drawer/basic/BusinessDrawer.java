package com.wayos.drawer.basic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import com.wayos.drawer.Canvas2D;
import com.wayos.drawer.Drawer;

public class BusinessDrawer extends Drawer {
	
	final String ENTITY_SEPARATOR = "\n\n";
	
	final String ENTITIES_GROUP_SEPARATOR = "~";
	
	final String MENU_ITEMS_SEPARATOR = "\n-";
	
	final String FIELD_SEPARATOR = "\n#";
	
	final String KEY_SEPARATOR = ",\n";
	
	final String FORWARD_SEPARATOR = "\n,";
	
	private final String content;
	
	private final Set<String> quizVarSet;
	
	private final Set<String> formVarSet;
	
	private String lastText;
	
	public BusinessDrawer(String content) {
		this.content = content;
		this.quizVarSet = new HashSet<>();
		this.formVarSet = new HashSet<>();
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
		
    	String [] entityGroups = content.split(ENTITY_SEPARATOR);
    	
    	Canvas2D.Entity [] parent = new Canvas2D.Entity [] { canvas2D.GREETING };
    	
    	/**
    	 * Unknown Logging
    	 */
    	Canvas2D.Entity unknown = canvas2D.newEntity(new Canvas2D.Entity [] { canvas2D.UNKNOWN }, "", "", "##", false);
		Canvas2D.Entity logUnknownEntity = canvas2D.newEntity(new Canvas2D.Entity[] { unknown }, "", "", "`?l_unknown=##`", false);
		canvas2D.nextRow(100);
		canvas2D.nextColumn(200);
		canvas2D.newEntity(new Canvas2D.Entity[] { logUnknownEntity }, "", "", null);
    	
    	Canvas2D.Entity resetScoreEntity = canvas2D.newEntity(parent, "welcome", "", false);
		canvas2D.nextRow(100);
		canvas2D.nextColumn(200);
		
		parent = new Canvas2D.Entity [] { resetScoreEntity };
		
		String [] tokens;
		
    	for (String entityTexts:entityGroups) {
    		
    		if (entityTexts.trim().isEmpty()) continue;//Skip empty line
    		
			tokens = entityTexts.split(ENTITIES_GROUP_SEPARATOR);
			
			if (tokens.length==0) {
				
				parent = createEntity(canvas2D, bundle, entityTexts, parent);
				
			} else {
				
				List<Canvas2D.Entity> parentGroup = new ArrayList<>();
				
				for (String entityText:tokens) {
					
					entityText = entityText.trim();
					
					parentGroup.addAll(Arrays.asList(createEntity(canvas2D, bundle, entityText, parent)));
					
				} 
				
				parent = parentGroup.toArray(new Canvas2D.Entity[parentGroup.size()]);	
				
			}
    		
    	}
    	
		canvas2D.nextColumn(300);
		
    	/**
    	 * Update clearSCoreEntity after collected varSet
    	 * `?<varName>=0 ..`
    	 */
		if (!quizVarSet.isEmpty() || !formVarSet.isEmpty()) {
			
	    	StringBuilder resetScoreExpressions = new StringBuilder();
	    	
	    	for (String var:quizVarSet) {
	    		resetScoreExpressions.append("`?");
	    		resetScoreExpressions.append(var);
	    		resetScoreExpressions.append("=0` ");
	    	}
			
	    	for (String var:formVarSet) {
	    		resetScoreExpressions.append("`?");
	    		resetScoreExpressions.append(var);
	    		resetScoreExpressions.append("=` ");
	    	}
	    	
	    	//System.out.println("clearScoreExpressions:" + clearScoreExpressions);
	    	resetScoreEntity.setExpressions(resetScoreExpressions.toString());
	    	resetScoreEntity.reAttachForwarder();//TODO: Reattach forwarder should move into setExpressions?
		}
		
		if (lastText==null) {
			lastText = bundle.getString("thread.end");
		}

    	/**
    	 * Log the summary with the last text edited by creator
    	 */    	    	
		lastText = lastText.replace("\n", "[br]");
		
    	String logExpression =  "`?l_thread=" + lastText + "`";
    	
		Canvas2D.Entity logEntity = canvas2D.newEntity(parent, "", "", logExpression, false);
		canvas2D.nextRow(100);
		canvas2D.nextColumn(200);
		
		canvas2D.newEntity(new Canvas2D.Entity[] { logEntity }, "", "", null);
		canvas2D.nextColumn(200);
    	
	}
	
	private Canvas2D.Entity [] createEntity(Canvas2D canvas2D, ResourceBundle bundle, String entityText, Canvas2D.Entity[] parent) {
		
		String keyword, responseText;
		String [] tokens;
		
		if (entityText.contains(MENU_ITEMS_SEPARATOR) || entityText.contains(FIELD_SEPARATOR)) {
			
	        parent = drawMenu(canvas2D, bundle, entityText, parent);
						
		} else {
			
			keyword = "";
			responseText = entityText;
			
			/**
			 * Map to keywords, responseText
			 */
			if (entityText.contains(KEY_SEPARATOR)) {
				
	    		tokens = entityText.split(KEY_SEPARATOR, 2);
	    		
				keyword = tokens[0].trim();
				responseText = tokens[1].trim();
				
				/**
				 * Add as another index to that path
				 */
				List<Canvas2D.Entity> parentList = new ArrayList<>(Arrays.asList(parent));
				parentList.add(canvas2D.newEntity(null, keyword, responseText, false));
				parent = parentList.toArray(new Canvas2D.Entity[parentList.size()]);
				
			} else {
				
				parent = new Canvas2D.Entity[] { canvas2D.newEntity(parent, keyword, responseText, false) };
			}
		
			
			/**
			 * Simple Entity
			 * set lastTextEntity to genenerate the log var
			 */
			lastText = responseText;
		}		
		
		canvas2D.nextColumn(200);
		canvas2D.nextRow(200);
		
		return parent;
	}

	private Canvas2D.Entity[] drawMenu(Canvas2D canvas2D, ResourceBundle bundle,
			String responseText, Canvas2D.Entity[] parent) {
		
		String [] tokens;
		
        List<Canvas2D.Entity> menuItemList = new ArrayList<>();
		
		/**
		 * If This menu has Keywords, Separate from main thread
		 */
		String keyword = "";
		if (responseText.contains(KEY_SEPARATOR)) {
			
			tokens = responseText.split(KEY_SEPARATOR, 2);
			keyword = tokens[0].trim();
			responseText = tokens[1].trim();
			
			for (Canvas2D.Entity thread:parent) {
				menuItemList.add(thread);				
			}			
			parent = null;
			
		}
		
		/**
		 * Map to keyword, responseText, choicesText for a question
		 */					
		String menuItemsText = "";	
		String varName = "";
		
		if (responseText.contains(MENU_ITEMS_SEPARATOR)) {
			
			tokens = responseText.split(MENU_ITEMS_SEPARATOR, 2);			
			responseText = tokens[0].trim();
			menuItemsText = tokens[1];	
			
			/**
			 * Extract varName
			 */
			if (menuItemsText.contains(FIELD_SEPARATOR)) {
				tokens = menuItemsText.split(FIELD_SEPARATOR, 2);
				menuItemsText = tokens[0].trim();
				varName = tokens[1].trim();
			}
			
		} else {
			
			/**
			 * Extract varName
			 */
			if (responseText.contains(FIELD_SEPARATOR)) {
				tokens = responseText.split(FIELD_SEPARATOR, 2);
				responseText = tokens[0].trim();
				varName = tokens[1].trim();
			}
			
		}
		
		/*
		System.out.println("Create Menu..");
		System.out.println("Keywords: " + keyword);
		System.out.println("Response: " + responseText);
		System.out.println("Choices: " + menuItemsText);
		System.out.println("Field: " +varName);
		System.out.println();
		*/
		
		String choiceJumpingText;
		
		canvas2D.nextColumn(200);
		canvas2D.nextRow(100);
						
		Canvas2D.Entity menu = canvas2D.newEntity(parent, keyword, responseText, true);
		
		canvas2D.nextColumn(300);
		
		if (!menuItemsText.isEmpty()) {
			
			tokens = menuItemsText.split(MENU_ITEMS_SEPARATOR);
	        
	        String exprs;
	        String [] t;
	        
	        for (String c:tokens) {
	        	
	        	c = c.trim(); //Trim Choice
	        	
	        	if (c.isEmpty()) continue; //Skip if empty
	        	
	        	/**
	        	 * Forwarding support , <jump to target keywords>
	        	 */
	        	if (c.contains(",")) {
	        		int lastIndexOfComma = c.lastIndexOf(",");
	        		choiceJumpingText = c.substring(lastIndexOfComma + 1).trim();
	        		c = c.substring(0, lastIndexOfComma);
	        	} else {
	        		choiceJumpingText = "";
	        	}
	        	
	        	/*
	        	System.out.println("Choice: " + c);
	        	System.out.println("\t>>" + forwardingText);
	        	*/
	   
	        	if (c.contains("#")) {
	        		
	        		keyword = c.substring(0, c.indexOf("#")).trim();
	            	if (keyword.isEmpty()) continue; //By the way skip the empty choice
	        		
	        		exprs = c.substring(c.indexOf("#")).trim();
	        		t = exprs.split(" ");
	        		
	        		responseText = "";
	        		for (String expr:t) {
	        			expr = expr.trim();
	        			if (expr.startsWith("#")) {
	        				expr = expr.substring(1);
	        				if (!expr.isEmpty()) {
	        					
	        					if (expr.contains("+")) {
	        						
	        						//Plus with amount of following number
	        						tokens = expr.split("\\+", 2);
	        						expr = tokens[0];
	        						int amount;
	        						try {
	        							amount = Integer.parseInt(tokens[1]);
	        						} catch (Exception e) {
	        							amount = 1;
	        						}
	                				responseText += " `?" + expr + "=+" + amount +"`";
	        						        						
	        					} else if (expr.contains("-")) {
	        						//Minus with amount of following number
	        						tokens = expr.split("-", 2);
	        						expr = tokens[0];
	        						int amount;
	        						try {
	        							amount = Integer.parseInt(tokens[1]);
	        						} catch (Exception e) {
	        							amount = 1;
	        						}
	                				responseText += " `?" + expr + "=-" + amount +"`";
	        						
	        					} else {
	        						//Reset to zero
	                				responseText += " `?" + expr + "=0`";
	        					}
	            				quizVarSet.add(expr);
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
	        	
	        	if (keyword.isEmpty()) continue; //By the way skip the empty choice
	        	
	    		canvas2D.nextRow(100);
	    		
	    		keyword = keyword.replace(System.lineSeparator(), " ");
	        	    		
	    		keyword = keyword.replace("*", "x");

	    		//keyword = keyword.replace(" ", "&nbsp;");

	    		//keyword = keyword.replace("+", "➕");
	    		
	    		//keyword = keyword.replace("-", "➖");
	    		
	        	if (!choiceJumpingText.isEmpty()) {
	        		
	        		responseText += " " + choiceJumpingText;
	        		
	        		Canvas2D.Entity entity = canvas2D.newEntity(new Canvas2D.Entity [] { menu }, keyword, "", responseText, null);
	        		entity.attachExpressionForLeaf();
	        		
	        	} else {
	        		
	            	menuItemList.add(canvas2D.newEntity(new Canvas2D.Entity [] { menu }, keyword, responseText, false));
	            	
	        	}
	        	
	        }			
		}		
        
		canvas2D.nextRow(100);
		
		/**
		 * Optional Input Answer Entity
		 */
		if (!varName.isEmpty()) {
						
    		menuItemList.add(canvas2D.newEntity(new Canvas2D.Entity [] { menu }, "", " `?" + varName + "=##`", false));
	    	
			formVarSet.add(varName);
			
		} else {
			
			//Empty Box
    		menuItemList.add(canvas2D.newEntity(new Canvas2D.Entity [] { menu }, "", "", false));
    		
		}
		
		canvas2D.nextColumn(200);
		
		Canvas2D.Entity knot = canvas2D.newEntity(menuItemList.toArray(new Canvas2D.Entity[menuItemList.size()]), "", "", false);
				        
        return new Canvas2D.Entity [] { knot };
    }

}