package com.wayos;

import java.io.Serializable;
import java.util.*;

import x.org.json.JSONArray;
import x.org.json.JSONObject;

/**
 * Created by eossth on 7/14/2017 AD.
 */
public class Node implements Serializable, Comparable<Node> {
	
	public static enum Type {
		LEAF, 
		FORWARDER,
		QUESTIONER
	}

    private List<Hook> hookList;

    private String response;

    private float active;

    private final Map<String, Object> attributes = new HashMap<>();

    public Node() {
    	
        this(new ArrayList<Hook>(), "");
    }

    /**
     * Clone from Node
     * 
     * @param node
     */
    public Node(Node node) {
    	
        this(new ArrayList<>(node.hookList), node.response);
        this.active = node.active;
        this.attributes.putAll(node.attributes);
        
    }

    public Node(List<Hook> hookList) {
    	
        this(hookList, "");
        
    }

    public Node(List<Hook> hookList, String response) {
    	
        this.hookList = hookList;
        this.response = response;
        
    }
    
    public static Node build(String [] hooks) {

        List<Hook> hookList = new ArrayList<>();

        if (hooks!=null) {
        	
            String hook;
            for (int i=0; i<hooks.length; i++) {
            	
                hook = hooks[i].trim();
                
                if (!hook.isEmpty()) {
                	
                    if (hook.startsWith(">="))
                        hookList.add(new NumberHook(hook.replace(">=",""), Hook.Match.GreaterEqualThan));
                    else if (hook.startsWith(">"))
                        hookList.add(new NumberHook(hook.replace(">", ""), Hook.Match.GreaterThan));
                    else if (hook.startsWith("<="))
                        hookList.add(new NumberHook(hook.replace("<=", ""), Hook.Match.LowerEqualThan));
                    else if (hook.startsWith("<"))
                        hookList.add(new NumberHook(hook.replace("<", ""), Hook.Match.LowerThan));
                    else
                        hookList.add(new KeywordsHook(hook, Hook.Match.Words));
                    
                }
            }
            
        }

        return new Node(hookList);
    }

    public static Node build(JSONObject jsonObject) {
    	
        JSONArray jsonArray = jsonObject.getJSONArray("hooks");
        
        List<Hook> hookList = new ArrayList<>();
        for (int i=0;i<jsonArray.length();i++) {
            hookList.add(Hook.build(jsonArray.getJSONObject(i)));
        }
        
        String response = jsonObject.getString("response");

        Node newNode = new Node(hookList, response);
        
        /**
         * Draw2D's Attributes for id, x, y and isQuestion
         */
        if (jsonObject.has("attr")) {
        	
            newNode.attributes.putAll(createDraw2DNodeAttributes(jsonObject.getJSONObject("attr")));
        }

        return newNode;
    }

    private static Map<String, Object> createDraw2DNodeAttributes(JSONObject jsonObject) {
    	
    	Map<String, Object> attributeMap = new HashMap<>();
    	
    	try { attributeMap.put("id", jsonObject.get("id")); } catch (Exception e) {}
    	try { attributeMap.put("x", jsonObject.get("x")); } catch (Exception e) {}
    	try { attributeMap.put("y", jsonObject.get("y")); } catch (Exception e) {}
    	try { attributeMap.put("isQuestion", jsonObject.get("isQuestion")); } catch (Exception e) {}
    	try { attributeMap.put("hasParam", jsonObject.get("hasParam")); } catch (Exception e) {}
    	
		return attributeMap;
	}

	public static JSONObject json(Node node) {
		
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        
        for (Hook hook:node.hookList) {
            jsonArray.put(Hook.json(hook));
        }
        
        jsonObject.put("hooks", jsonArray);
        jsonObject.put("response", node.response);
        jsonObject.put("attr", node.attributes);
        
        return jsonObject;
    }
	
	public Map<String, Object> attr() {
		
		return attributes;
	}
	
	public Object attr(String name) {
	
		return attributes.get(name);
	}
	
	public void attr(String name, Object value) {
		
		attributes.put(name, value);
	}
	
	public String id() {
		
		return (String) attributes.get("id");
	}

    public boolean coverHooks(Node fromNode) {
    	
        return hookList.containsAll(fromNode.hookList);
        
    }

    public boolean sameHooks(Node anotherNode) {
    	
        return hookList.equals(anotherNode.hookList);
        
    }

    public void addHook(Node fromNode) {
    	
        for (Hook fromHook:fromNode.hookList) {
        	
            if (!hookList.contains(fromHook)) {
            	
                hookList.add(Hook.build(fromHook.text, fromHook.match));
            }
        }
        
    }

    public void addHook(String input, Hook.Match match) {
    	
        hookList.add(Hook.build(input, match));
        
    }
    
    public void setResponse(String text) {

    	response = text;
    	
    }

    public boolean matched(MessageObject messageObject) {
    	
        for (Hook hook:hookList) {
        	
            if (hook.matched(messageObject)) {
            	
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 
     * Scoring = Intersection / Input + Hooks (Union Area)
     * 
     * hooks = hello world
     * words = hello
     * 1 / (2 + 1 - 1) = 0.5
     */
    public void feed(MessageObject messageObject) {
    	   
        int wordCount = messageObject.wordCount();

        int hookCount = 0;
        int parentCount = 0;
        int matchedCount = 0;
        float totalResponseActive = 0;
        
        for (Hook hook:hookList) {
        	
            if (hook.matched(messageObject)) {
            	
                totalResponseActive += hook.weight;
                matchedCount ++;
                
            }
            
            /**
             * TODO: How to score * matched
             */
            
            if (hook.text.startsWith("@")) {
            	
                parentCount = 1;//Maximum Parent Count is 1
                
            } else {
            	
                hookCount ++;
                
            }
        } 
        
        int allCount;
        
        allCount = (hookCount + parentCount + wordCount - matchedCount); //Union
                
        active = totalResponseActive / allCount;

        /**
         * DEBUG
         */
        /*
        if (matchedCount != _matchedCount || totalResponseActive != _totalResponseActive) {
            System.out.println("CKKKKKKKKK!!!!!!!");        	
        }
        
        System.out.println("---" + messageObject.wordList());
        System.out.println("hookCount:" + hookCount);
        System.out.println("parentCount:" + parentCount);
        System.out.println("wordCount:" + wordCount);
        System.out.println("matchedCount:" + matchedCount);
        System.out.println("_matchedCount:" + _matchedCount);
        System.out.println("totalResponseActive:" + totalResponseActive);
        System.out.println("_totalResponseActive:" + _totalResponseActive);
        System.out.println("active:" + active);
        System.out.println("---");
        */
    }
    
    /**
     * 
     * Get matchedCount from "matchedHookList" Attribute, So no need to call hook.matched(messageObject) again
     * 
     * Scoring = Intersection / Input + Hooks (Union Area)
     * 
     * hooks = hello world
     * words = hello
     * 1 / (2 + 1 - 1) = 0.5
     *
     * @param messageObject
     * 
     */
    public void fast_feed(MessageObject messageObject) {
    	
        int wordCount = messageObject.wordCount();
        
        int matchedIndexCount = 0;
        
        float totalMatchedIndexResponseActive = 0.0f;
        
        int hookCount = 0;//TODO should be add keyCount or not?
        
        List<Hook> matchedHookList = (List<Hook>) messageObject.attr("matchedHookList");        
        
        if (matchedHookList!=null) {
        	
        	matchedIndexCount = matchedHookList.size();
        	
            for (Hook hook:matchedHookList) {
            	
            	totalMatchedIndexResponseActive += hook.weight;
            	
            }
        }
                
        int parentCount = 0;
        int matchedCount = matchedIndexCount;
        float totalResponseActive = totalMatchedIndexResponseActive;        
        
        for (Hook hook:hookList) {
        	
            /**
             * For Special Hook such as..
             * KeywordHook, Ex matched * ,
             * NumberHook, Ex matched gt lt
             */
        	if (matchedHookList == null || !matchedHookList.contains(hook)) {
        		
                if (hook.matched(messageObject)) {
                	
                	totalResponseActive += hook.weight;
                	matchedCount ++;
                    
                }
                
        	}
        	
        	if (hook.text.startsWith("@")) {
            	
                parentCount = 1;//Maximum Parent Count is 1
                
            } else {
            	
                hookCount ++;
                
            }
        } 
        
        int allCount = (hookCount + parentCount + wordCount - matchedCount); //Union
        
        active = totalResponseActive / allCount;
        
        /**
         * DEBUG
         *      
         	hookCount:1
			parentCount:0
			wordCount:1
			matchedCount:1
			totalResponseActive:1.0
			active:1.0
			hookCount:1
			parentCount:0
			wordCount:1
			matchedCount:1
			totalResponseActive:1.0
			active:1.0

         */
        
        /*
        System.out.println("---" + this);
        
        System.out.println("input:" + messageObject.wordList());
        
        System.out.println("matchedHookList:" + matchedHookList);
        System.out.println("matchedIndexCount:" + matchedIndexCount);
        System.out.println("totalMatchedIndexResponseActive:" + totalMatchedIndexResponseActive);        
        
        System.out.println("hookCount:" + hookCount);
        System.out.println("parentCount:" + parentCount);
        System.out.println("wordCount:" + wordCount);
        System.out.println("matchedCount:" + matchedCount);
        System.out.println("totalResponseActive:" + totalResponseActive);
        System.out.println("allCount:" + allCount);        
        System.out.println("active:" + active);
        
        System.out.println("---");
        */
      
    }    

    public void feedback(MessageObject messageObject, float feedback) {

        for (Hook hook:hookList) {
            if (hook.matched(messageObject)) {
                hook.feedback(feedback);
            }
        }
        
    }

    public List<Hook> hookList() {
    	
        return hookList;
        
    }

    public void release() {
    	
        active = 0;        
    }

    public void release (float rate) {
    	
        active *= rate;
        
    }

    public String response() {
    	
        return response;

    }

    public float active() {
    	
        return active;
        
    }

    @Override
    public String toString() {
    	
        return Hook.toString(hookList) + System.lineSeparator() + response;
    }
    
    /**
     * To clean hooks for Matched Head Node.
     * @param input
     * @return
     */
    public String cleanHooksFrom(String input) {
    	
    	if (input==null) input = "";

        for (Hook hook:hookList) {
            if (input.startsWith(hook.text)) {
                input = input.substring(hook.text.length()).trim();
            }
        }

        return input.trim();
    }

    public String clean(String [] inputs) {

        StringBuilder cleanInput = new StringBuilder();
        
        boolean matched;
        for (String input:inputs) {

            matched = false;
            for (Hook hook:hookList) {
                if (hook.text.equals(input)) {
                    matched = true;
                    break;
                }
            }

            if (!matched) cleanInput.append(input + " ");
        }
        
        return cleanInput.toString().trim();
    }

    @Override
    public int hashCode() {
    	
        return Objects.hash(hookList, response);
        
    }

    @Override
    public boolean equals(Object obj) {
    	
        if (obj instanceof Node) {
            Node another = (Node)obj;
            return Objects.equals(hookList, another.hookList) && Objects.equals(response, another.response);
        }
        
        return false;
    }
    
    /**
     * Id Comparasion
     */
    public boolean hasSameId(Node anotherNode) {
    	
    	if (this.attributes.get("id")==null) return false;
    	
    	if (anotherNode.attributes.get("id")==null) return false;
    	
        return this.attributes.get("id").equals(anotherNode.attributes.get("id"));
        
    }

    /**
     * if (response.contains(", ")) {
        	
        	if (response.endsWith("?"))
        		return Type.QUESTIONER;
        	
        	if (response.endsWith("!"))
        		return Type.FORWARDER;
        }

     * @return
     */
	public Type type() {
		
        Boolean isQuestion = (Boolean) attributes.get("isQuestion");
		
    	if (isQuestion!=null) {
    		
    		if (isQuestion)
    			return Type.QUESTIONER;
    		
    		return Type.FORWARDER;
    		
    	}
    	
        Boolean hasParam = (Boolean) attributes.get("hasParam");
        
        if (hasParam!=null && hasParam) {
        	
    		return Type.FORWARDER;
    		
        }
    
		return Type.LEAF;    	
	}
	
	/**
	 * Compare HookList
	 */
	@Override
	public int compareTo(Node otherNode) {
				
		return Hook.toString(this.hookList).compareTo(Hook.toString(otherNode.hookList));
	}

}
