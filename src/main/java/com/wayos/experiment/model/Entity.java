package com.wayos.experiment.model;

import java.util.ArrayList;
import java.util.List;

import com.wayos.expression.Expression;

public class Entity {
	
	private final Container container;
	
	private List<String> keyList;
	
	private List<Expression> expressionList;
	
	private Content content;
	
	public Entity(Container container) {
		
		this.container = container;
		this.container.add(this);
		
		keyList = new ArrayList<>();
		expressionList = new ArrayList<>();
	}
	
	public List<String> keyList () {
		
		return keyList;
	}
	
	public float matchedScore(String message) {
		
		boolean matched;
		
		float matchedScore;
		
		float maxScore = 0;
		
		/**
		 * Find Max score matched
		 */
		for (String key:keyList) {
			
			matched = false;
			
			if (key.startsWith("*") && key.endsWith("*")) {
				
				key = key.substring(1, key.length());
				
				matched = message.contains(key);
				
			} else if (key.startsWith("*")) {
				
				key = key.substring(1);
				
				matched = message.endsWith(key);
				
			} else if (key.endsWith("*")) {
				
				key = key.substring(0, key.length());
				
				matched = message.startsWith(key);
				
			} else {
				
				matched = message.equals(key);
				
			}
			
			if (matched) {
				
				/**
				 * Ex. 
				 * message = "Hello how are you"
				 * 
				 * key1 = Hello*
				 * matchedScore = 5 / 17
				 * 
				 * key2 = how are
				 * matchedScore = 7 / 17 <= maxScore
				 */
				matchedScore = key.length() / message.length();
				
				if (matchedScore > maxScore) {
					
					maxScore = matchedScore;
					
				}
				
			}
			
		}
				
		return maxScore;
				
	}
	
	public Result call(String message) {
		
		
		
		return null;
	}

}
