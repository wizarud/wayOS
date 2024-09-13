package com.wayos;

import java.util.List;

public class KeywordsHook extends Hook {

    public KeywordsHook(String text, Match match) {
        super(text, match);
    }

    public KeywordsHook(String text, Match match, double weight) {
        super(text, match, weight);
    }
    
    @Override
    public boolean matched(MessageObject messageObject) {
    	
        if (!messageObject.isSplitted()) {
            messageObject.split();
        }

        List<String> wordList = messageObject.wordList();

        String input = messageObject.toString();

        //System.out.println("Check Matching:" + input + " vs " + text);
            	            	
        //For Keywords Match!
        /**
         * Ex 
         * hook texts: สวัสดี,ครับ,ไง*
         * input: [ดี, ครับ]	
         */
                
        if (text.contains(",")) {
        	
            String[] tokens = text.toLowerCase().split(",");
            
            for (String token : tokens) {
            	
            	token = token.trim();
            	
            	/**
            	 * Parameterized Splitted Keyword
            	 */
            	token = super.parameterized(messageObject);
            	
            	for (String word:wordList) {
            		
                    if (starMatched(word, token)) {
                    	return true;
                    }
                    
                    if (word.equalsIgnoreCase(token)) {
                        return true;
                    }
                    
            	}
                
            }
        }
            	
        /**
         * Parameterized Single Hook
         */
    	String text = super.parameterized(messageObject);
    	
    	for (String word:wordList) {
    		
            if (starMatched(word, text)) {
            	return true;
            }
            
            if (word.equalsIgnoreCase(text)) {
                return true;
            }
                      
    	}
    	
        if (starMatched(input, text)) {
        	return true;
        }

    	
        if (input.equalsIgnoreCase(text)) {
        	return true;
        }
        
    	return false;
    }
    
    /**
     * Support Simple Regx Markup *
     */
    private boolean starMatched(String input, String key) {
    	    	
    	if (key.equals("*")) {
    		
    		//System.out.println(">>" + input);
    		
    		//Must be word or number
    		return !input.startsWith("@") && !input.startsWith("#");   		
    		
    	}
    	
    	if (key.length()>3 && key.startsWith("*") && key.endsWith("*")) {
    		        	
    		return input.toLowerCase().contains(key.substring(1, key.length()-1).toLowerCase());
    	}
    	
    	if (key.startsWith("*")) {
        	return input.toLowerCase().endsWith(key.substring(1).toLowerCase());
    	}
    	
    	if (key.endsWith("*")) {
    		return input.toLowerCase().startsWith(key.substring(0, key.length()-1).toLowerCase());
    	}
    	
    	return false;
    }

}