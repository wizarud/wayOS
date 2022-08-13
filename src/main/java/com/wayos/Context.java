package com.wayos;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.BreakIterator;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by eoss-th on 8/15/17.
 */
@SuppressWarnings("serial")
public abstract class Context implements Serializable {

	public static final List<String> RESERVED_PROPERTIES = Arrays.asList("greeting", "unknown", "silent");
	
    public static final String SUFFIX = ".context";

    protected ReadWriteLock lock = new ReentrantReadWriteLock();
    
    private Locale locale = Locale.getDefault();

    private List<String> adminIdList = new ArrayList<>();
    
    private Map<String, String> properties = new HashMap<>();

    private Map<String, Object> attributes = new HashMap<>();

    private List<Node> nodeList = new ArrayList<>();
    
    /**
     * Index for Soft key Node such as *
     */
    private List<Node> softNodeList = new ArrayList<>();
    
    /**
     * index for Hard Key Node
     */
    private Map<String, List<Node>> indexMap = new HashMap<>();

    private final String name;

    private ContextListener listener;

    public Context(String name) {
    	
        this.name = name;
    }

    public Context contextListener(ContextListener listener) {
    	
        this.listener = listener;
        
        return this;
    }

    public Context locale(Locale locale) {
    	
        this.locale = locale;
        
        return this;
    }

    public Locale locale() {
    	
        return locale;
        
    }

    public boolean isAdmin(String userId) {
    	
        return adminIdList.contains(userId);
        
    }

    public void load() throws Exception {
    	
        load(name);
        
    }

    public void load(String name) throws Exception {
    	
        lock.readLock().lock();
        
        try {
        	
        	/**
        	 * Clear All meta data
        	 */
        	properties.clear();
        	attributes.clear();
        	
            doLoad(name);
            
            /**
             * Assign Language
             */
            String language = prop("language");
            if (language==null) {
            	language = "en";
            }
            locale(new Locale(language));            
            
        } finally {
        	
            lock.readLock().unlock();
        }
        
    }

    public void save() {
    	
        save(name);
        
    }

    public void save(String name) {
    	
        lock.readLock().lock();
        
        boolean saved = false;
        
        try {
        	
            doSave(name, nodeList);
            
            saved = true;
            
        } finally {
        	
            lock.readLock().unlock();
            
            if (saved && listener!=null)
                listener.callback(new NodeEvent(MessageObject.build(name), NodeEvent.Event.ContextSaved));
            
        }
    }

    public final void loadJSON(String jsonString) {
    	
        JSONObject object = new JSONObject(jsonString);
        
        Set<String> propertyNames = object.keySet();
        
        for (String property:propertyNames) {
        	
            if (property.equals("nodes")) {
            	
                nodeList.clear();
                nodeList.addAll(build(object.getJSONArray(property)));
                
            } else if (property.equals("attr")) {
            	
            	attributes.putAll(createDraw2DContextAttributes(object.getJSONObject("attr")));
                
            } else {
            	
                try {
                	
                    properties.put(property, object.getString(property));
                    
                } catch (JSONException e) {
                    continue;
                }
                
            }
        }
        
        /**
         * TODO: generate index from special hooks first (*matching)
         */
        
        /**
         * Generate Spread Index from all hooks (normal text) to own node
         * in Many to Many relation form
         * 
         * Ex
         * 
         * From node List
         * [hooks] [node]
		 * [aaa, bbb] => xxx
		 * [bbb] => yyy
		 * [bbb, ccc] => zzz
		 * 
		 * To index Map
		 * aaa => [xxx]
		 * bbb => [xxx, yyy, zzz]
		 * ccc => [zzz]
		 * 
		 * For search and feed matched nodes
         */
        indexMap.clear();
        String key;
        List<Node> memberList;
        for (Node node:nodeList) {
        	
        	for (Hook hook:node.hookList()) {
        		
        		key = hook.toString().toLowerCase(); //MessageOblect is splitt to lowercase, So prevent the capital letters
        		
        		//Generate Index for Soft Key Nodes
        		if (key.startsWith("*") || key.endsWith("*") || key.contains(",")) {
        			
        			softNodeList.add(node);
        			
        		} else {
        			
        			//Generate Index for Hard Key Nodes
            		memberList = indexMap.get(key);
            		
            		if (memberList==null) {
            			
            			memberList = new ArrayList<>();
            			
            		}
            		
        			memberList.add(node);
            		indexMap.put(key, memberList);
            		
        		}
        		
        	}
        }
        
    }

    private Map<String, Object> createDraw2DContextAttributes(JSONObject object) {
    	
    	Map<String, Object> contextAttributeMap = new HashMap<>();
    	
    	try {
    		
        	JSONArray connections = object.getJSONArray("connections");
        	List<Map<String, String>> connectionMapList = new ArrayList<>();
        	
        	Map<String, String> connectionMap;
        	JSONObject obj;
        	for (int i=0; i<connections.length(); i++) {
        		
        		obj = connections.getJSONObject(i);
        		
        		connectionMap = new HashMap<>();
        		connectionMap.put("id", obj.getString("id"));
        		connectionMap.put("source", obj.getString("source"));
        		connectionMap.put("target", obj.getString("target"));
        		
        		connectionMapList.add(connectionMap);
        	}
        	
        	contextAttributeMap.put("connections", connectionMapList);
    		
    	} catch (JSONException e) {
    		
    	}
    	    	
    	try {
    		
    		JSONObject jsonObject = object.getJSONObject("start");
        	Map<String, Object> attributeMap = new HashMap<>();    	
        	attributeMap.put("id", jsonObject.get("id"));
        	attributeMap.put("x", jsonObject.get("x"));
        	attributeMap.put("y", jsonObject.get("y"));
        	attributeMap.put("isQuestion", jsonObject.get("isQuestion"));
        	
        	contextAttributeMap.put("start", attributeMap);
        	
    	} catch (JSONException e) {
    		
    	}
    	
    	try {
    		
    		JSONObject jsonObject = object.getJSONObject("end");
        	Map<String, Object> attributeMap = new HashMap<>();    	
        	attributeMap.put("id", jsonObject.get("id"));
        	attributeMap.put("x", jsonObject.get("x"));
        	attributeMap.put("y", jsonObject.get("y"));
        	attributeMap.put("isQuestion", jsonObject.get("isQuestion"));
        	
        	contextAttributeMap.put("end", attributeMap);
        	
    	} catch (JSONException e) {
    		
    	}
    	
    	try {
    		
    		JSONObject jsonObject = object.getJSONObject("silent");
        	Map<String, Object> attributeMap = new HashMap<>();    	
        	attributeMap.put("id", jsonObject.get("id"));
        	attributeMap.put("x", jsonObject.get("x"));
        	attributeMap.put("y", jsonObject.get("y"));
        	attributeMap.put("isQuestion", jsonObject.get("isQuestion"));
        	
        	contextAttributeMap.put("silent", attributeMap);
        	
    	} catch (JSONException e) {
    		
    	}
    	
		return contextAttributeMap;
	}
    
    public String name() {
   
    	return name;
    }
    
    public ContextListener listener() {
    	
    	return listener;
    }
    
    public List<Node> nodeList() {
    	
    	return nodeList;
    }
    
	public Map<String, String> prop() {
		
		return properties;
	}
	
	public String prop(String name) {
	
		return properties.get(name);
	}
	
	public void prop(String name, String value) {
		
		properties.put(name, value);
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

	public final String toJSONString() {
		
        JSONObject object = new JSONObject();
        
        for (Map.Entry<String, String> entry:properties.entrySet()) {
        	
            object.put(entry.getKey(), entry.getValue());
        }
        
        object.put("attr", new JSONObject(attributes));
        object.put("nodes", json(nodeList));
        
        return object.toString();
    }

    public Node get(List<Hook> hookList) {
    	
        lock.readLock().lock();
        
        try {
        	
            for (Node node:nodeList) {
            	
                if (node.hookList().equals(hookList))
                    return node;
                
            }
            
        } finally {
        	
            lock.readLock().unlock();
        }
        
        return null;
    }

    public void add(Node newNode) {
    	
        lock.writeLock().lock();
        
        try {
        	
            nodeList.add(newNode);
            
        } finally {
        	
            lock.writeLock().unlock();
            
        }
    }

    public void clear() {
    	
        lock.writeLock().lock();
        
        try {
        	
            nodeList.clear();
            
        } finally {
        	
            lock.writeLock().unlock();
            
        }
    }

    @Override
    public String toString() {
    	
        lock.readLock().lock();
        
        StringBuilder data = new StringBuilder();
        
        try {
        	
            for (Node node: nodeList) {
            	
                data.append(node);
                data.append(System.lineSeparator());
                
            }
            
        } finally {
        	
            lock.readLock().unlock();
        }
        return data.toString();
    }

    public static List<Node> build(JSONArray jsonArray) {
    	
        List<Node> nodeList = new ArrayList<>();
        
        for (int i=0;i<jsonArray.length();i++) {
        	
            nodeList.add(Node.build(jsonArray.getJSONObject(i)));
        }
        
        return nodeList;
    }

    public static JSONArray json(List<Node> nodeList) {
    	
        JSONArray jsonArray = new JSONArray();
        
        for (Node node:nodeList) {
        	
            jsonArray.put(Node.json(node));
            
        }
        
        return jsonArray;
    }
    
    public boolean matched(MessageObject messageObject, ContextListener listener) {
    	
        Set<Node> matchedNodeSet = new HashSet<>();
        
        boolean matched = false;
        
        /**
         * Soft Key Node Searching
         */
        for (Node node:softNodeList) {
        	
        	if (node.matched(messageObject)) {
        		
        		matched = true;
        		
        		matchedNodeSet.add(node);        		
        	}
        }
        
        List<String> wordList = messageObject.wordList();
                
        List<Node> memberList;
                        
        /**
         * Hard Key Node Searching
         */
        for (String word:wordList) {
        	        	
        	memberList = indexMap.get(word);
        	
        	if (memberList!=null) {
        		
                matched = true;
                
                matchedNodeSet.addAll(memberList);
                                
        	}
        }
        
		for (Node node:matchedNodeSet) {
			
            listener.callback(new NodeEvent(node, messageObject.copy(), NodeEvent.Event.Matched));
			
		}
    	
    	return matched;
    }
    
    public boolean matched(MessageObject messageObject, List<Node> nodeList, ContextListener listener) {
    	
        boolean matched = false;
        
        for (Node node:nodeList) {
        	
        	/**
        	 * Clone only matched node
        	 */
            if (node.matched(messageObject)) {

            	NodeEvent nodeEvent = new NodeEvent(node, messageObject.copy(), NodeEvent.Event.Matched);
            	
                listener.callback(nodeEvent);
                matched = true;
                
            } 
        }
        
        return matched;
    }

    public Node build(MessageObject messageObject) {

        String input = messageObject.toString();

        Node node = Node.build(split(input));

        Object mode = messageObject.attr("mode");

        if (mode!=null && !mode.toString().trim().isEmpty()) {
        	
            node.addHook(mode.toString().trim(), Hook.Match.Mode);
        }

        return node;
    }
    
    public Node build(String keywords) {

        return Node.build(split(keywords));
    }

    public String [] split(String input) {
    	
        return split(input, locale);
        
    }

    private String [] split(String input, Locale locale) {
    	
        BreakIterator breakIterator = BreakIterator.getWordInstance(locale);
        
        List<String> result = new ArrayList<>();

        /**
         * Conditional Hooks
         */
        List<String> conditionHooks = new ArrayList<>();

        List<String> variableHooks = new ArrayList<>();

        List<String> parentHooks = new ArrayList<>();

        String [] tokens = input.split(" ");
        String hook, subHook;
        int wordBoundaryIndex, prevIndex;
        
        for (String token:tokens) {
        	
            hook = token.trim();
            
            if (hook.startsWith(">") ||
                    hook.startsWith(">=") ||
                    hook.startsWith("<") ||
                    hook.startsWith("<=")) {
            	
                conditionHooks.add(hook);
                
            } else if (hook.startsWith("#")) {
            	
                variableHooks.add(hook);
                
            } else if (hook.startsWith("@") && !hook.substring(1).trim().isEmpty() && hook.contains("-")) {
            	
                parentHooks.add(hook);
                
            } else if (hook.startsWith("https://") || hook.startsWith("http://")) {
            	
            	result.add(hook);
            	
            } else {
                /**
                 * Sentence
                 */
                breakIterator.setText(hook);

                wordBoundaryIndex = breakIterator.first();
                prevIndex         = 0;
                int subWordCount  = 0;
                
                while(wordBoundaryIndex != BreakIterator.DONE) {
                	
                    subHook = hook.substring(prevIndex, wordBoundaryIndex).trim();
                    
                    if (!subHook.isEmpty() && !subHook.equals("@")/* Hot Fix to protect Bug from email! */) {
                        result.add(subHook);
                        subWordCount ++;
                    }
                    
                    prevIndex = wordBoundaryIndex;
                    wordBoundaryIndex = breakIterator.next();
                }

                if (subWordCount>1) {
                    result.add(hook);
                }

            }
        }
        
        result.addAll(conditionHooks);
        result.addAll(variableHooks);
        result.addAll(parentHooks);
        
        return result.toArray(new String[result.size()]);
    }

    public boolean isEmpty() {
    	
        lock.readLock().lock();
        
        try {
        	
            return nodeList.isEmpty();
            
        } finally {

        	lock.readLock().unlock();
        }
    }
    
    /**
     * For internal replacement by session
     * @return
     */
    public final Map<String, String> reversedOrderPropertiesMap() {
    	
        Map<String, String> propertiesMap = new TreeMap<>(Collections.reverseOrder());
        
        for(Map.Entry<String, String> entry: properties.entrySet()) {
        	
        	if (RESERVED_PROPERTIES.contains(entry.getKey())) {
        		
        		continue;
        	}
        	
        	propertiesMap.put("$" + entry.getKey(), entry.getValue());
        }
    	
    	return propertiesMap;
    }
    
    /**
     * For export to csv
     * @return
     */
    public final Map<String, String> sortedPropertiesMap() {
    	
        Map<String, String> propertiesMap = new TreeMap<>();
        
        for(Map.Entry<String, String> entry: properties.entrySet()) {
        	
        	if (RESERVED_PROPERTIES.contains(entry.getKey())) {
        		
        		continue;
        	}
        	
        	propertiesMap.put(entry.getKey(), entry.getValue());
        }
    	
    	return propertiesMap;
    }
    
    /**
     * For variables removing
     * remove persistance session vars if not start with #<NAME>???
     * @return
     */
    public final Set<String> variablesSet() {
    	
    	Set<String> variablesSet = new HashSet<>();
    	
    	Pattern pattern = Pattern.compile("`\\?(\\w+)=");
    	
    	String response;
    	Matcher matcher;
    	String token;
    	
    	for (Node node:nodeList) {
    		
    		response = node.response();
            matcher = pattern.matcher(response);
            
            while (matcher.find()) {
            	
              token = matcher.group(1);
              if (!token.isEmpty()) {
            	  variablesSet.add("#" + token);
              }
              
            }
            
    	}
    	    	
    	return variablesSet;
    }
    
    protected abstract void doLoad(String name) throws Exception;

    protected abstract void doSave(String name, List<Node> nodeList);

}
