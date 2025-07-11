package com.wayos.drawer.basic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.wayos.drawer.Canvas2D;
import com.wayos.drawer.Drawer;

public class WayDrawer extends Drawer {
	
	final String ENTITY_SEPARATOR = "\n\n";
	
	final String MENU_ITEMS_SEPARATOR = "\n-";
	
	final String KEY_SEPARATOR = ",\n";
	
	private final String content;
	
	private final Set<String> quizVarSet;
	
	private final Set<String> formVarSet;
	
	public WayDrawer(String content) {
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
		
    	String [] entities = content.split(ENTITY_SEPARATOR);
    	
    	Canvas2D.Entity [] parent = new Canvas2D.Entity [] { canvas2D.GREETING };
    	
    	
    	Canvas2D.Entity resetScoreEntity = canvas2D.newEntity(null, "welcome", "", false);
		canvas2D.nextRow(100);
		canvas2D.nextColumn(200);
		
		parent = new Canvas2D.Entity [] { resetScoreEntity };
		
		int i = 0;
		
		String parentBot = null;
		
    	for (String entity:entities) {
    		
    		entity = entity.trim();
    		
    		if (entity.isEmpty()) continue;//Skip empty line
    		
    		try {
    			
        		/*
        		 * Inheritance support by unknown forward to parent botName
        		 */
        		if (i==0 && 
        				!entity.contains(" ") && 
        				!entity.contains("\n") && 
        				entity.startsWith("[") &&
        				entity.endsWith("]")    				
         				) {
        			
        			parentBot = entity.substring(1, entity.length()-1);
        			
        			Canvas2D.Entity unknown;
        			        			
        			//Is Webhook service bot, Call as Post API
        			if (parentBot.startsWith("http://") || parentBot.startsWith("https://")) {
        				
        				String url = parentBot.split("://")[1] + (parentBot.startsWith("http://") ? "-nonsecure":"");
        				String body = "message=##&sessionId=#sessionId";
        				
                		unknown = canvas2D.newEntity(new Canvas2D.Entity [] { canvas2D.UNKNOWN }, "", "", "`post://" + body + "://" + url + "`", false);
        	    		canvas2D.nextColumn(200);
        	    		
        			} else {
        				
                		unknown = canvas2D.newEntity(new Canvas2D.Entity [] { canvas2D.UNKNOWN }, "", "", "`cmd://call://" + parentBot + " ##`", false);
        				
        			}        			
        			
            		canvas2D.nextColumn(200);
            		
        	    	canvas2D.newEntity(new Canvas2D.Entity [] { unknown }, "", "%1", null);
        	    	
        			canvas2D.nextRow(100);
        			canvas2D.nextColumn(200);
        			
        			continue;
        		}
        		
    			parent = createEntity(canvas2D, entity, parent);
    			
    		} finally {
    			
    			i++;
    			
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
		
    	/**
    	 * Log the summary with the last text edited by creator
    	 */    	    	
		/*
		//lastText = lastText.replace("\n", "[br]");
		
    	String logExpression =  "`?l_way=" + lastText + "`";
    	
		Canvas2D.Entity logEntity = canvas2D.newEntity(parent, "", "", logExpression, false);
		canvas2D.nextRow(100);
		canvas2D.nextColumn(200);
		*/
		
		//String adsText = bundle.getString("way.ads");
		String adsText = "";
		
		canvas2D.newEntity(parent, "", adsText, null);
		
    	/**
    	 * Greeting to welcome and Logging Unknown
    	 */
    	if (parentBot==null) {
    		
    		canvas2D.setPosition(300, 200);
    		
        	canvas2D.newEntity(new Canvas2D.Entity [] { canvas2D.GREETING }, "", "", "welcome", null).attachExpressionForLeaf();
        	
    		canvas2D.nextRow(100);
    		
        	/**
        	 * Unknow forwarding to blabla
        	 */
        	Canvas2D.Entity unknown = canvas2D.newEntity(new Canvas2D.Entity [] { canvas2D.UNKNOWN }, "", "", "##", false);
        	
    		canvas2D.nextColumn(200);
    		
    		canvas2D.newEntity(new Canvas2D.Entity[] { unknown }, "", "", "blabla ##", null).attachExpressionForLeaf();
    		
    	} else {
    		
    		canvas2D.setPosition(300, 200);
    		
    		//Bypass Greeting to parent, forward to welcome later
			Canvas2D.Entity greeting;
			
			//Is Webhook service bot, Call as Post API
			if (parentBot.startsWith("http://") || parentBot.startsWith("https://")) {
				
				String url = parentBot.split("://")[1] + (parentBot.startsWith("http://") ? "-nonsecure":"");
				String body = "message=greeting&sessionId=#sessionId";
				
				greeting = canvas2D.newEntity(new Canvas2D.Entity [] { canvas2D.GREETING }, "", "", "`post://" + body + "://" + url + "`", false);
	    		canvas2D.nextColumn(200);
				
			} else {
				
				greeting = canvas2D.newEntity(new Canvas2D.Entity [] { canvas2D.GREETING }, "", "", "`cmd://call://" + parentBot + " ##`", false);
				
			}
			
    		canvas2D.nextColumn(200);
    		
	    	canvas2D.newEntity(new Canvas2D.Entity [] { greeting }, "", "%1", "welcome", null).attachExpressionForLeaf();
		
    	}
    	
	}
	
	private Canvas2D.Entity [] createEntity(Canvas2D canvas2D, String entityText, Canvas2D.Entity[] parent) {
		
		String keywords, responseText;
		String [] tokens;
		
		if (entityText.contains(MENU_ITEMS_SEPARATOR)) {
			
	        parent = drawMenu(canvas2D, entityText, parent);
						
		} else {
			
			keywords = "";
			responseText = entityText;
			
			List<Canvas2D.Entity> parentList;
			Canvas2D.Entity newEntity;
			
			/**
			 * Map to keywords, responseText
			 */
			if (entityText.contains(KEY_SEPARATOR)) {
				
	    		tokens = entityText.split(KEY_SEPARATOR, 2);
	    		
				keywords = tokens[0].trim();
				responseText = "\n" + tokens[1].trim();
				
				/**
				 * Save current parent to the list
				 */
				parentList = new ArrayList<>(Arrays.asList(parent));
				parent = null;
				
			} else {
				
				parentList = new ArrayList<>();
				
			}
			
			String varsText = "";
			String jumpingText = "";
			
			if (responseText.contains("\n:")) {
								
				tokens = responseText.split("\n:");
				
				responseText = tokens[0].trim();//Body 
				varsText = tokens[1].trim();
				
	        	// Forwarding support , <jump to target keywords>
	        	 
	        	if (varsText.contains(",")) {
	        		int lastIndexOfComma = varsText.lastIndexOf(",");
	        		jumpingText = varsText.substring(lastIndexOfComma + 1).trim();
	        		varsText = varsText.substring(0, lastIndexOfComma);
	        	} else {
	        		jumpingText = "";
	        	}
								
			} else if (responseText.contains("\n,")) {
				
				tokens = responseText.split("\n,");
				
				responseText = tokens[0].trim();
				jumpingText = tokens[1].trim();
				
			} else if (responseText.startsWith(":")) {
				
				//For empty responseText but has vars or contains jumping text
				
				varsText = responseText.substring(1);
				responseText = "";
				
	        	/**
	        	 * Forwarding support , <jump to target keywords>
	        	 */
	        	if (varsText.contains(",")) {
	        		
	        		int lastIndexOfComma = varsText.lastIndexOf(",");
	        		jumpingText = varsText.substring(lastIndexOfComma + 1).trim();
	        		varsText = varsText.substring(0, lastIndexOfComma);
	        		
	        	} else {
	        		
	        		jumpingText = "";
	        	}
								
			} else if (responseText.startsWith(",")) {
				
				//For empty responseText but has jumping text
				
				tokens = responseText.split(",");
				
				responseText = tokens[0].trim();
				jumpingText = tokens[1].trim();
				
			} 
			
			System.out.println("Create Simple Text..");
			System.out.println("Keywords: " + keywords);
			System.out.println("Response: " + responseText);
			System.out.println("Vars Text: " + varsText);
			System.out.println("Jumping Text: " + jumpingText);
			System.out.println();
			
			String expressions = "";
			
			if (!varsText.isEmpty()) {
				
				expressions = toExpressions(varsText, false);
			}
			
			tokens = keywords.split(",");
			
			List<Canvas2D.Entity> keywordEntityList = new ArrayList<>();
			
			String params;
			
			for (String keyword:tokens) {
				
				keyword = keyword.trim();
				
				if (keyword.isEmpty()) {
					
					params = "";
					
				} else {
					
					params = "##";
					
				}
				
				keywordEntityList.add(canvas2D.newEntity(parent, keyword, "", params, false));
				
				canvas2D.nextRow(100);
				
			}
			
			Canvas2D.Entity [] keywordEntities = keywordEntityList.toArray(new Canvas2D.Entity[keywordEntityList.size()]);
			
        	if (!jumpingText.isEmpty()) {
        		
        		expressions += " " + jumpingText;
        		
        		newEntity = canvas2D.newEntity(keywordEntities, "", responseText, expressions, null);
        		newEntity.attachExpressionForLeaf();
        		
        	} else {
        		
    			newEntity = canvas2D.newEntity(keywordEntities, "", responseText + expressions, false);			
    			parentList.add(newEntity);
            	
        	}
			
			parent = parentList.toArray(new Canvas2D.Entity[parentList.size()]);
					
		}
		
		canvas2D.nextColumn(150);
		canvas2D.nextRow(100);
		
		return parent;
	}

	private Canvas2D.Entity[] drawMenu(Canvas2D canvas2D, String responseText, Canvas2D.Entity[] parent) {
		
		String [] tokens;
		
        List<Canvas2D.Entity> menuItemList = new ArrayList<>();
		
		/**
		 * If This menu has Keywords, Separate from main way
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
		
		if (responseText.contains(MENU_ITEMS_SEPARATOR)) {
			
			tokens = responseText.split(MENU_ITEMS_SEPARATOR, 2);			
			responseText = tokens[0].trim();
			menuItemsText = tokens[1];	
			
		} 
		
		System.out.println("Create Menu..");
		System.out.println("Keywords: " + keyword);
		System.out.println("Response: " + responseText);
		System.out.println("Choices: " + menuItemsText);
		System.out.println();
		
		String choiceJumpingText;
		
		canvas2D.nextColumn(200);
		canvas2D.nextRow(100);
						
		Canvas2D.Entity menu = canvas2D.newEntity(parent, keyword, responseText, true);
		
		canvas2D.nextColumn(300);
		
		if (!menuItemsText.isEmpty()) {
			
			tokens = menuItemsText.split(MENU_ITEMS_SEPARATOR);
	        
	        String varsText;
	        
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
	        		
	        		varsText = c.substring(c.indexOf("#")).trim();
	        		
	        		responseText = toExpressions(varsText, keyword.isEmpty());
	        		
	        	} else {
	        		
	        		keyword = c;        		
	        		responseText = "";
	        		
	        	}
	        	
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
		
		canvas2D.nextColumn(200);
		
		Canvas2D.Entity knot = canvas2D.newEntity(menuItemList.toArray(new Canvas2D.Entity[menuItemList.size()]), "", "", false);

        return new Canvas2D.Entity [] { knot };
    }
	
	private String toExpressions(String varsText, boolean isStringDefault) {
		
		String [] tokens;
		String [] vars = varsText.split(" ");
		
		String expressions = "";
		
		for (String var:vars) {
			
			var = var.trim();
			
			if (var.startsWith("#")) {
				
				var = var.substring(1);
				
				if (!var.isEmpty()) {
					
					if (var.contains("+")) {
						
						//Plus with amount of following number
						tokens = var.split("\\+", 2);
						var = tokens[0];
						int amount;
						try {
							amount = Integer.parseInt(tokens[1]);
						} catch (Exception e) {
							amount = 1;
						}
        				expressions += " `?" + var + "=+" + amount +"`";
						        						
					} else if (var.contains("-")) {
						//Minus with amount of following number
						tokens = var.split("-", 2);
						var = tokens[0];
						int amount;
						try {
							amount = Integer.parseInt(tokens[1]);
						} catch (Exception e) {
							amount = 1;
						}
        				expressions += " `?" + var + "=-" + amount +"`";
						
					} else if (var.contains("=")) {
						
						//Assignment
						tokens = var.split("=", 2);
						var = tokens[0];
						try {
							int amount = Integer.parseInt(tokens[1]);
	        				expressions += " `?" + var + "=" + amount +"`";
						} catch (Exception e) {
	        				expressions += " `?" + var + "=" + tokens[1] +"`";
						}
						
					}
					
					else {
						//Reset to zero
        				expressions += " `?" + var + "=0`";
					}
					
					if (isStringDefault) {
						
			        	/**
			        	 * For Alternative answer, Use it instead of zero
			        	 */	
		        		expressions = expressions.replace("=0", "=##");
		    			formVarSet.add(var);
		    			
					} else {
						
        				quizVarSet.add(var);
        				
					}
				}
				
			} else if (!var.isEmpty()) {
				
				expressions += " " + var;
				
			} 
		}	
		
		return expressions.trim();
	}

}