package com.wayos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * Encapsulation of message with embedded attributes
 * 
 * @author Wisarut Srisawet
 *
 */
public class MessageObject implements Serializable {

    private final Map<String, Object> attributes;

    private MessageObject(Map<String, Object> attributes) {

        if (attributes==null)
            this.attributes = new HashMap<>();
        else
            this.attributes = attributes;

    }

    public static MessageObject build() {
    	
        return build("");
        
    }

    public static MessageObject build(String text) {
    	
        return new MessageObject(updateAttributes(null, text));
        
    }

    public static MessageObject build(Map<String, Object> attributes) {
    	
        return new MessageObject(new HashMap<>(attributes));
        
    }

    public static MessageObject build(MessageObject fromMessageObject, String overrideText) {
    	
        return new MessageObject(updateAttributes(fromMessageObject.attributes, overrideText));
        
    }

    public MessageObject split() {
    	
        String text = attributes.get("text").toString();
        
        List<String> wordList = Arrays.asList(text.toLowerCase().split("\\s+"));
        
        attributes.put("wordList", wordList);
        attributes.put("wordCount", wordList.size());
        
        return this;
    }
    
    public boolean isSplitted() {
    	
        return attributes.get("wordList") != null;
        
    }
    
    public int wordCount() {
    	
    	int length;
    	
        Integer wordCount = (Integer) attributes.get("wordCount");
        
        if (wordCount!=null) {
        	
            length = wordCount;
            
        } else if (!toString().trim().isEmpty()) {
        	
        	length = 1;
            
        } else {
        	
        	length = 0;
            
        }
        
        return length;
    }
    
    public Object attr(String key) {
    	
    	return attributes.get(key);
    }
    
    public MessageObject attr(String key, Object val) {
    	
    	attributes.put(key, val);
    	
    	return this;
    }

    public MessageObject setText(String text) {
    	
        updateAttributes(attributes, text);
        
        /**
         * TODO: Clear splitted because of new text;
         */
        attributes.remove("wordList");
        attributes.remove("wordCount");
        
        return this;
    }
    
    public MessageObject append(String word) {
    	
    	String thisText = toString() + word;
    	
    	setText(thisText);
    	
    	return this;
    }

    public MessageObject split(Context context) {
    	
        String text = attributes.get("text").toString();
        List<String> wordList = Arrays.asList(context.split(text.toLowerCase()));
        attributes.put("wordList", wordList);
        
        attributes.put("wordCount", wordList.size());
        
        int wordCount = 0;
        for (String word:wordList) {
        	if (word.startsWith("@")) continue;
        	wordCount ++;
        }
        attributes.put("wordCount",wordCount);
        
        return this;
    }

    public MessageObject copy() {
    	
        return new MessageObject(new HashMap<>(attributes));
    }

    private static Map<String, Object> updateAttributes(Map<String, Object> attributes, String text) {

        if (attributes == null) {
            attributes = new HashMap<>();
        }
        
        if (text == null) {
        	text = "";
        }        
        
        String head, tail;
        
        int lastIndexOfComma = text.lastIndexOf(", @");
        
        if (lastIndexOfComma == -1) {
        	
            lastIndexOfComma = text.lastIndexOf(", ");

            if (lastIndexOfComma == -1) {
            	
                head = tail = "";
                
            } else {
            	
                head = text.substring(0, lastIndexOfComma).trim();

                if (text.endsWith("!"))
                	tail = text.substring(lastIndexOfComma + 1, text.length()-1).trim();
                else
                	tail = text.substring(lastIndexOfComma + 1).trim();
            }            
            
        } else {
        	
            head = text.substring(0, lastIndexOfComma).trim();

            if (text.endsWith("!"))
                tail = text.substring(lastIndexOfComma + 2, text.length()-1).trim();
            else
                tail = text.substring(lastIndexOfComma + 2).trim();
            
        }

        attributes.put("text", text);
        attributes.put("head", head);
        attributes.put("tail", tail);
        
        /*
        System.out.println("Text:" + text);
        System.out.println("Tail:" + tail);
        System.out.println();
        */
        
        return attributes;
    }

    public String head() {
    	
        String head = (String) attributes.get("head");
        return head==null || head.isEmpty()? "" : head;
        
    }

    public String headIncluded() {
    	
        String head = (String) attributes.get("head");
        return head==null || head.isEmpty()? "" : head + "\n\n\n";
        
    }

    public String tail() {
    	
        String tail = (String) attributes.get("tail");
        return tail==null ? "" : tail;
        
    }

    public MessageObject clean() {

        setText(toString().
                replace(", ", " ").
                replace("!", "").
                replace("?", ""));

        return this;
    }

    public MessageObject forward() {
    	
        updateAttributes(attributes, (String) attributes.get("tail"));
        
        return this;
    }

    public MessageObject forward(String forwardedFrom) {
    	
        updateAttributes(attributes, (String) attributes.get("tail"));
        
        attributes.put("forwardedFrom", forwardedFrom);        
        
        return this;
    }
    
    public boolean forwardedFrom(String from) {
    	
    	if (from==null) return false;
    	
        String forwardedFrom = (String) attributes.get("forwardedFrom");
        
        if (forwardedFrom==null) return false;
        
        return forwardedFrom.equals(from);
    }
    
    @Override
    public String toString() {
    	
        Object text = attributes.get("text");
        
        return text!=null?text.toString():"";
        
    }
    
    @Override
    public boolean equals(Object o) {
    	
    	if (o instanceof MessageObject) {
    		
    		MessageObject anotherMessageObject = (MessageObject) o;
    		
    		return this.attributes.equals(anotherMessageObject.attributes);
    	}
    	
		return false;
    }
	
	public List<String> wordList() {
		
        List<String> wordList = (List<String>) attributes.get("wordList");
        
        if (wordList!=null) {
        	
        	return new ArrayList<>(wordList);
        }
        
        return new ArrayList<>();
	}
	
	public List<String> resultList() {
		
        List<String> resultList = (List<String>) attributes.get("resultList");
        
        if (resultList!=null) {
        	
        	return new ArrayList<>(resultList);
        }
        
        return new ArrayList<>();
	}
	
	public void addResult(String result) {
		
        List<String> resultList = (List<String>) attributes.get("resultList");
        
        if (resultList==null) {
        	
        	resultList = new ArrayList<>();
        	            
            attributes.put("resultList", resultList);
            
        }
        
        resultList.add(result);
        
        //System.out.println("Expression RESULT:" + result);
        //System.out.println("Expression resultList:" + resultList);
        
    
	}

}
