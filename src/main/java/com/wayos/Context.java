package com.wayos;

import java.io.Serializable;
import java.text.BreakIterator;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import x.org.json.JSONArray;
import x.org.json.JSONException;
import x.org.json.JSONObject;

/**
 * Created by Wisarut Srisawet on 8/15/17.
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
    private Map<String, Map<Node, Hook>> indexMap = new HashMap<>();

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
             * I move config locale property to loadJSON for indexMap setting!!!
             */
                        
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
    	
    	synchronized(nodeList) {
    		
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
             * Assign Language (Moved from load())
             */
            String language = prop("language");
            if (language==null) {
            	language = "en";
            }
            locale(new Locale(language));
            
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
            softNodeList.clear();
            
            String key;
            Map<Node, Hook> memberList;
            String [] keyArray;
            for (Node node:nodeList) {
            	
            	for (Hook hook:node.hookList()) {
            		
            		if (hook instanceof NumberHook) continue;
            		
            		key = hook.toString().toLowerCase(); //MessageOblect is splitt to lowercase, So prevent the capital letters
            		
            		//Generate Index for Soft Key Nodes
            		if (key.startsWith("*") || key.endsWith("*") /*|| key.contains("|")*/) {
            			
            			softNodeList.add(node);
            			
            		} else {
            			            			
            			keyArray = this.split(key, locale);//TODO: Must load locale first!!!
            			
            			for (String k:keyArray) {
            				
                			//Generate Index for Hard Key Nodes
                    		memberList = indexMap.get(k);
                    		
                    		if (memberList==null) {
                    			
                    			memberList = new HashMap<>();
                    			
                    		}
                    		
                    		//System.out.println("Making Index.." + k + ":" + hook.weight + "/" + keyArray.length);
                    		
                			memberList.put(node, Hook.build(hook.text, hook.match, hook.weight / keyArray.length));
                    		indexMap.put(k, memberList);
                    	
            			}
            		
            		}
            		
            	}
            }
            
    	}
    	            
        //debug();        
    }
    
    private void debug() {
    	
    	System.out.println("Watching index..");
    	
    	for (Map.Entry<String, Map<Node, Hook>> k:indexMap.entrySet()) {
    		
    		System.out.print(k.getKey());
    		System.out.print("\t");
    		
    		for (Map.Entry<Node, Hook> v:k.getValue().entrySet()) {
    			
    			System.out.print(v.getKey().toString());
        		System.out.print("\t");
    			System.out.print(v.getValue().toString());
    			
    		}
    		
    		System.out.println();
    		System.out.println();
    		
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
        	
        	if (jsonObject.opt("isQuestion")!=null) {
            	attributeMap.put("isQuestion", jsonObject.get("isQuestion"));
        	}
        	
        	contextAttributeMap.put("start", attributeMap);
        	
    	} catch (JSONException e) {
    		
    	}
    	
    	try {
    		
    		JSONObject jsonObject = object.getJSONObject("end");
        	Map<String, Object> attributeMap = new HashMap<>();    	
        	attributeMap.put("id", jsonObject.get("id"));
        	attributeMap.put("x", jsonObject.get("x"));
        	attributeMap.put("y", jsonObject.get("y"));
        	
        	if (jsonObject.opt("isQuestion")!=null) {
            	attributeMap.put("isQuestion", jsonObject.get("isQuestion"));
        	}
        	
        	contextAttributeMap.put("end", attributeMap);
        	
    	} catch (JSONException e) {
    		
    	}
    	
    	try {
    		
    		JSONObject jsonObject = object.getJSONObject("silent");
        	Map<String, Object> attributeMap = new HashMap<>();    	
        	attributeMap.put("id", jsonObject.get("id"));
        	attributeMap.put("x", jsonObject.get("x"));
        	attributeMap.put("y", jsonObject.get("y"));
        	
        	if (jsonObject.opt("isQuestion")!=null) {
            	attributeMap.put("isQuestion", jsonObject.get("isQuestion"));
        	}
        	
        	contextAttributeMap.put("silent", attributeMap);
        	
    	} catch (JSONException e) {
    		
    		//e.printStackTrace();
    		
    	}
    	
    	//System.out.println(contextAttributeMap.toString());
    	
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
        
        //System.out.println(wordList);
                
        Map<Node, Hook> memberList;
                        
        /**
         * Hard Key Node Searching
         */
        Map<Node, List<Hook>> nodeMatchedHookListMap = new HashMap<>();
        Hook hook;
        List<Hook> hookList;
        
        for (String word:wordList) {
        	        	
        	memberList = indexMap.get(word);
        	
        	if (memberList!=null) {
        		
                matched = true;
                
                matchedNodeSet.addAll(memberList.keySet());
                
                for (Node node:memberList.keySet()) {
                	
                	hookList = nodeMatchedHookListMap.get(node);
                	
                	if (hookList==null) {
                		hookList = new ArrayList<>();
                	}
                	
                	hook = memberList.get(node);
                	
                	hookList.add(hook);
                	
                	nodeMatchedHookListMap.put(node, hookList);
                }
        
        	}
        }
        
        //System.out.println("M" + matchedNodeSet);
        
        MessageObject msg;
        
        List<Hook> matchedHookList;
        
		for (Node node:matchedNodeSet) {
			
			msg = messageObject.copy();
			
			matchedHookList = nodeMatchedHookListMap.get(node);
			
			msg.attr("matchedHookList", matchedHookList);
			
            listener.callback(new NodeEvent(node, msg, NodeEvent.Event.Matched));
			
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
    	
    	//System.out.println("Splitting" + input);
    	
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

                /*
                if (subWordCount>1) {
                    
                    result.add(hook);
                }
                */

            }
        }
        
        result.addAll(conditionHooks);
        result.addAll(variableHooks);
        result.addAll(parentHooks);
        
        //System.out.println(result);
        
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
        
        //System.out.println("sortedPropertiesMap(): " + properties.toString());
        
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
    
    public void validate() {
    	
    	if (this.attributes==null || this.attributes.isEmpty()) return; //Skip
    	
        lock.readLock().lock();
        
        System.out.println("----- Start Validation " + name + " -----");
        
        try {
        	
        	Set<String> sourceConnectionSet = new HashSet<>();
        	
        	List<Map<String, String>> connectionMapList = (List<Map<String, String>>) this.attributes.get("connections");
        	
        	for (Map<String, String> connectionMap:connectionMapList) {
        		
        		sourceConnectionSet.add(connectionMap.get("source"));
        		
        	}
        	
        	//System.out.println("Sources Connection:" + sourceConnectionSet);
        	
        	boolean isValid = true;
        	
            for (Node node: nodeList) {
            	
            	if (node.attr("isQuestion")!=null && (boolean) node.attr("isQuestion")==false) {
            		
            		/**
            		 * Check what is forwarded from this node?
            		 */
            		if (!sourceConnectionSet.contains(node.id())) {
            			
            			if (node.response().contains(", @")) {
            				
            				isValid = false;
            				
                			System.out.println(node.id() + ">>" + node.response());
            				
            			}
            			
            		}
            		
            	}
            	
            }
                        
            if (isValid) {
            	
            	System.out.println("OK");
            	
            }
            
            if (attributes.get("silent")==null) {
            	
            	System.err.println(name + " missing silent.." + attributes.get("silent"));
            	
            }
            
            
        } finally {
        	
            lock.readLock().unlock();
            
            System.out.println("----- End Validation -----");
        }
    	
    	
    }

}
