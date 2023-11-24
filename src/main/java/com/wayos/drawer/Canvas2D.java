package com.wayos.drawer;

import org.json.JSONObject;

import com.wayos.Context;
import com.wayos.Hook;
import com.wayos.Node;
import com.wayos.Hook.Match;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Canvas2D {

    public static class Entity {
		
        public Node node;
        
        public String expressions;
        
        public Entity(String keywords, String responseText, Boolean isQuestion) {
        	
        	/**
        	 * Extract expression from responseText
        	 */
            Pattern pattern = Pattern.compile("\\`(.|\\n|\\r|\\t)*?\\`", Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(responseText);

            StringBuilder expressionStrings = new StringBuilder();
            String evaluatedText = responseText;
            String expression;
            while (matcher.find()) {
                expression = matcher.group();
                evaluatedText = evaluatedText.replace(expression, "");
                expressionStrings.append(expression + " ");
            }
                        
            this.constructor(keywords, evaluatedText.trim(), expressionStrings.toString().trim(), isQuestion);            
        }
        
        public Entity(String keywords, String answer, String expressions, Boolean isQuestion) {

            this.constructor(keywords, answer, expressions, isQuestion);
        }
                
        private void constructor(String keywords, String answer, String expressions, Boolean isQuestion) {
        	
            this.node = new Node();
            
            String [] hookTexts = keywords.split(" ");
            
            for (String keyword:hookTexts) {
            	
            	keyword = keyword.trim();
            	
                if (!keyword.equals("GREETING") && !keyword.equals("UNKNOWN") && !keyword.isEmpty()) {
                	
                	Hook.Match match;
                	
                	if (keyword.startsWith("gte")) {
                		
                		match = Hook.Match.GreaterEqualThan;
                		
                		keyword = keyword.replace("gte", "");
                		
                	} else if (keyword.startsWith("gt")) {
                		
                		match = Hook.Match.GreaterThan;
                		
                		keyword = keyword.replace("gt", "");
                		
                	} else if (keyword.startsWith("lte")) {
                		
                		match = Hook.Match.LowerEqualThan;
                		
                		keyword = keyword.replace("lte", "");
                		
                	} else if (keyword.startsWith("lt")) {
                		
                		match = Hook.Match.LowerThan;
                		
                		keyword = keyword.replace("lt", "");
                		
                	} else {
                		
                		match = Hook.Match.Words;
                		
                	}
                	
                    this.node.addHook(keyword, match);
                }
            	
            }

            this.node.attr("id", UUID.randomUUID().toString());
            //this.node.attributes.put("id", createShortID()); TODO: cannot load FIX THIS!
            
            if (isQuestion!=null) {
            	
                this.node.attr("isQuestion", isQuestion);            	
            }
            
        	/**
        	 * Clean Tab to 3 spaces, to protect bug from CSVWrapper
        	 */
            answer = answer.replace("\t", "   ");
            answer = answer.trim();
                                    
            this.node.setResponse(answer);
            this.expressions = expressions.trim();
        }        
        
        public String id() {
        	
            return (String) node.attr("id");
        }

        public boolean hasExpression() {
        	
        	return !expressions.isEmpty();
        }
        
        /**
         * 
         * @return true if response is end with , @id `expression<N>`! (to prevent add marker again!)
         */
        public boolean hasForwarder() {
        	
        	String response = node.response();
        	
        	if (response==null) return false;
        	
            int lastIndexOfComma = response.lastIndexOf(", @");
            
            if (lastIndexOfComma==-1) return false;
            
            String id = (String) node.attr("id");
            
            if (id==null) return false;
            
        	String tail = response.substring(lastIndexOfComma + 1).trim();
        	
        	return tail != null && tail.startsWith("@" + id) && tail.endsWith(getMarker());
        }

		public String getMarker() {
			
			Node.Type type = node.type();
			
			if (type==Node.Type.QUESTIONER)
				return "?";
			
			if (type==Node.Type.FORWARDER)
				return "!";
			
			return "";
		}

		public void attachForwarder() {
			
        	if (!hasForwarder()) {
        		
                if (hasExpression()) {
                	
                    node.setResponse(node.response() + ", @" + id() + " " + expressions + getMarker());

                } else {

                    node.setResponse(node.response() + ", @" + id() + getMarker());

                }
                
        	} 
			
		}
		
		public void reAttachForwarder() {
			
			if (!hasForwarder()) {
				
				attachForwarder();
				
				return;
			}
			
			String responseText = node.response();
			
			responseText = responseText.substring(0, responseText.lastIndexOf(", @"));
			
            if (hasExpression()) {
            	
                node.setResponse(responseText + ", @" + id() + " " + expressions + getMarker());
                
            } else {
            	
                node.setResponse(node.response() + ", @" + id() + getMarker());
                
            }
		            
		}
		
		public void attachExpressionForLeaf() {
			
            if (hasExpression()) {
            	
    			String responseText = node.response();
    			    			
        		boolean hasVariableForwarder = false;
        		
        		String [] tokens = expressions.split(" ");        		
        		
        		for (String token:tokens) {
        			token = token.trim();
        			if (!token.startsWith("`") && !token.endsWith("`")) {
        				hasVariableForwarder = true;
        			};
        			
        		}
        		
        		/*
    			System.out.println("Attach Expression For Leaf Node..");
    			System.out.println("ResponseText: " + responseText);
    			System.out.println("Expression: " + expressions);
    			System.out.println("hasVariableForwarder: " + hasVariableForwarder);    			
    			System.out.println();
    			*/
    			
        		if (hasVariableForwarder) {
        			
        			node.attr("hasParam", true);
                    node.setResponse(responseText + ", " + expressions + "!");
                    
        		} else {
        			
                    node.setResponse(responseText + " " + expressions);
        			
        		}

            }
			
		}
		
		public void setQuestion(Boolean isQuestion) {
			
            this.node.attr("isQuestion", isQuestion);
            
		}
		
		public void setExpressions(String expressions) {
			
            this.expressions = expressions.trim();        	
			
		}

    }

    public final Context context;

    public Entity GREETING;

    public Entity UNKNOWN;

    public Entity SILENT;

    private int x;
    private int y;

    public Canvas2D(Context context, String title, int shift, boolean clear) {

        this.context = context;
        
        x = shift;
        y = shift;
        
        if (clear) {
        	
        	String borderColor =  this.context.prop("borderColor");
        	if (borderColor == null) {
        		borderColor = "#64c583";
        	}
        	
        	String language =  this.context.prop("language");
        	if (language == null) {
        		language = "en";
        	}
        	
        	System.out.println("Clearing Context..");
        	
        	this.context.prop().clear();
        	this.context.attr().clear();
        	this.context.clear();
        	
            GREETING = new Entity("GREETING", "", null);
            GREETING.node.attr("x", x);
            GREETING.node.attr("y", y);
            this.context.attr("start", new JSONObject(GREETING.node.attr()));
            this.context.prop("greeting", "");
            y += shift;

            UNKNOWN = new Entity("UNKNOWN", "", null);
            UNKNOWN.node.attr("x", x);
            UNKNOWN.node.attr("y", y);
            this.context.attr("end", new JSONObject(UNKNOWN.node.attr()));
            this.context.prop("unknown", "");
            y += shift;

            SILENT = new Entity("SILENT", "", null);
            SILENT.node.attr("x", x);
            SILENT.node.attr("y", y);
            this.context.attr("silent", new JSONObject(SILENT.node.attr()));
            this.context.prop("silent",  "");
            y += shift;

            this.context.prop("title", title);
            this.context.prop("borderColor", borderColor);
            this.context.prop("language", language);
        }

    }

    private static String createShortID() {
    	
    	String [] tokens = UUID.randomUUID().toString().split("-");
    	//return RandomStringUtils.random(8, elements);        	
    	
    	return tokens[tokens.length-1];
    }
    
    /**
     * db.newEntity(GREETING, "").newEntity("", "").newQuestion("", [{},{},{}])
     * @return
     */
    public Entity newEntity(Entity [] parentEntities, String keyword, String response, Boolean isQuestion) {

        Entity newEntity = new Entity(keyword, response, isQuestion);

        drop(parentEntities, newEntity);

        return newEntity;
    }
    
    public Entity newEntity(Entity [] parentEntities, String keyword, String response, String expressions, Boolean isQuestion) {

        Entity newEntity = new Entity(keyword, response, expressions, isQuestion);

        drop(parentEntities, newEntity);

        return newEntity;
    }
    
    public void drop(Entity [] parentEntities, Entity newEntity) {
    	
        newEntity.node.attr("x", x);
        newEntity.node.attr("y", y);

        bind(parentEntities, newEntity);

        this.context.add(newEntity.node);    	
    }

	public void bind(Entity[] parentEntities, Entity entity) {
		
		if (parentEntities!=null) {

            for (Entity parentEntity:parentEntities) {

            	List<Map<String, String>> connections = (List<Map<String, String>>) context.attr("connections");
                
                if (connections == null) {
                	
                	connections = new ArrayList<>();
                } 

                Map<String, String> connectionObj = new HashMap<>();
                connectionObj.put("id", UUID.randomUUID().toString());
                //connectionObj.put("id", createShortID()); TODO: Cannot greeting, Fix THis!!
                connectionObj.put("source", (String) parentEntity.node.attr("id"));
                connectionObj.put("target", (String) entity.node.attr("id"));

                context.attr("connections", connections);
                
                connections.add(connectionObj);

                if (parentEntity==GREETING) {

                    this.context.prop("greeting", ", @" + parentEntity.node.attr("id") + "!");                    
                    
                    ((JSONObject)this.context.attr("start")).put("isQuestion", false);

                } else if (parentEntity==UNKNOWN) {

                    this.context.prop("unknown", ", @" + parentEntity.node.attr("id") + "!");
                    
                    ((JSONObject)this.context.attr("end")).put("isQuestion", false);
                    
                } else {
                	
            		parentEntity.attachForwarder();
                	
                }

                entity.node.addHook("@" + parentEntity.node.attr("id"), Hook.Match.Words);
            }

        }
	}
    
    public void nextColumn(int shift) {
        x += shift;
    }

    public void nextRow(int shift) {
        y += shift;
    }
    
    public void setPosition(int x, int y) {
    	this.x = x;
    	this.y = y;
    }
    
    public int getX() {
    	return this.x;
    }
    
    public int getY() {
    	return this.y;
    }
    
    public int getMaxY() {
    	
    	List<Node> nodeList = this.context.nodeList();
    	
    	Integer maxY = 0;
    	
    	Integer y;
    	for (Node node:nodeList) {
    		
    		try {
    			
    			y = (Integer) node.attr("y");
    			
    			if (y>maxY) {
    				maxY = y;
    			}
    			
    		} catch (Exception e) {
    			
    			e.printStackTrace();
    			continue;
    			
    		}
    		
    	}
    	
    	return maxY;
    	
    }
    
    private boolean isChild(Node parentNode, Node childNode) {
    	
        String id = (String) parentNode.attr("id");   
        
        if (id==null) return false;
        
		for (Hook hook:childNode.hookList()) {

			if (hook.text.equals("@" + id)) {
				return true;
			}

		}
            	
    	return false;
    }
        
    private void recursiveCollectChild(List<Node> nodeList, Map<Node, Set<Node>> parentMap, Node reachNode) {
    	
    	Set<Node> parentSet;
    	
    	for (Node node:nodeList) {
    		
    		if (reachNode==node) continue;
    		
    		if (isChild(reachNode, node)) {
    			
    			parentSet = parentMap.get(node);
    			    			
    			if (parentSet==null) {
    				
    				parentSet = new HashSet<>();
    			}
    			
    			if (parentSet.add(reachNode)) {
    				
        			parentMap.put(node, parentSet);
        			
        			recursiveCollectChild(nodeList, parentMap, node);
    				
    			}    			
    			
    		}
    	}
    
    }
    
    private String hooksToKeywords(List<Hook> hookList) {
    	
		String label = "";
		
		String prefix;
		for (Hook hook:hookList) {

			//Ignore Parent
			if (hook.text.startsWith("@")) {
				continue;
			}
			
			prefix = "";
			
			if (hook.match==Match.GreaterEqualThan) {
				prefix = "gte";
			} else if (hook.match==Match.GreaterThan) {
				prefix = "gt";
			} else if (hook.match==Match.LowerEqualThan) {
				prefix = "lte";
			} else if (hook.match==Match.LowerThan) {
				prefix = "lt";
			}

			label += prefix + hook.text + " ";
		}

		label = label.trim();
    	
		return label;
    }    
    
    private String applyParameters(String source, Map<String, String> paramMap) {
    	
    	String result = source;
    	
    	if (paramMap!=null) {
        	for (Map.Entry<String, String> entry:paramMap.entrySet()) {
        		result = result.replace(entry.getKey(), entry.getValue());
        	}	
    	}
    	
    	return result;
    }
    
    public void clone(Context fromContext, String rootKeywords, Map<String, String> paramMap) {
    	
    	List<Node> matchedNode = new ArrayList<>();
    	
    	String label;
    	
    	List<Node> sourceNodeList;
    	
    	if (fromContext!=null) {
    		
    		sourceNodeList = fromContext.nodeList();
    		
    	} else {
    		
    		sourceNodeList = this.context.nodeList();    		
    		
    	}    	
    	
    	for (Node node:sourceNodeList) {
    		
			label = this.hooksToKeywords(node.hookList());
			
			//Whole Matched 
			if (rootKeywords.equals(label)) {
				
				matchedNode.add(node);
			}
    		
    	}
    	
    	Map<Node, Set<Node>> parentMap = new HashMap<>();
    	
    	for (Node node:matchedNode) {
    		
    		recursiveCollectChild (sourceNodeList, parentMap, node);
    	}
    	
    	matchedNode.addAll(parentMap.keySet());
    	
    	if (matchedNode.isEmpty()) throw new IllegalArgumentException("Missmatched keyword " + rootKeywords);
    	
    	/**
    	 * Clone
    	 */
    	
    	int xOffSet = 0;
    	int yOffSet = 0;
    	int maxY = this.getMaxY();
    	
    	Entity newEntity;
    	
    	String keywords, responseText;
    	Boolean isQuestion;
    	    	    	    	
    	
    	/**
    	 * Mapping between node and new clone Entity
    	 */
    	Map<Node, Entity> nodeEntityMap = new HashMap<>();
    	
    	int x, y;
    	
    	for (Node cloneFromThisNode:matchedNode) {
    		
    		keywords = this.hooksToKeywords(cloneFromThisNode.hookList());
    		
    		isQuestion = (Boolean) cloneFromThisNode.attr("isQuestion");
    		
    		responseText = cloneFromThisNode.response();
    		
    		x = (int) cloneFromThisNode.attr("x") + xOffSet;
    		y = (int) cloneFromThisNode.attr("y") + maxY + yOffSet;
    		
            keywords = applyParameters(keywords, paramMap);
            responseText = applyParameters(responseText, paramMap);
            
    		newEntity = newEntity(null, keywords, responseText, isQuestion);
    		newEntity.node.attr("x", x);
    		newEntity.node.attr("y", y);
    		
            if (responseText.contains(", ") && (responseText.endsWith("!") || responseText.endsWith("?"))) {
            	responseText = responseText.replace((String) cloneFromThisNode.attr("id"), newEntity.id());
            }  
            
            newEntity.node.setResponse(responseText);    		
    		
    		nodeEntityMap.put(cloneFromThisNode, newEntity);
    		
    	}
    	
    	/**
    	 * Create Connections
    	 */    	    	
    	Entity entity;
    	Entity [] parentEntities;
    	int i;
    	for (Map.Entry<Node, Set<Node>> entry:parentMap.entrySet()) {
    		
    		entity = nodeEntityMap.get(entry.getKey());
    		
    		parentEntities = new Entity[entry.getValue().size()];
    		i = 0;
    		for (Node node:entry.getValue()) {
    			parentEntities[i++] = nodeEntityMap.get(node);
    		}
    		
    		this.bind(parentEntities, entity);
    	}
    	    	
    }
    
    public void edit(String rootKeywords, Map<String, String> keywordsResponseMap) {
    	
    	List<Node> matchedNode = new ArrayList<>();
    	
    	String label;
    	
    	for (Node node:this.context.nodeList()) {
    		
			label = this.hooksToKeywords(node.hookList());
			
			//Whole Matched 
			if (rootKeywords.equals(label)) {
				
				matchedNode.add(node);
			}
    		
    	}
    	
    	Map<Node, Set<Node>> parentMap = new HashMap<>();
    	
    	for (Node node:matchedNode) {
    		
    		recursiveCollectChild (this.context.nodeList(), parentMap, node);
    	}
    	
    	matchedNode.addAll(parentMap.keySet());
    	
    	
    	/**
    	 * Editing
    	 */
    	
    	String keywords, oldResponseText, newResponseText;
    	for (Node editThisNode:matchedNode) {
    		
    		keywords = this.hooksToKeywords(editThisNode.hookList());
    		
            newResponseText = keywordsResponseMap.get(keywords);
            
            if (newResponseText!=null) {
            	
        		if (editThisNode.type()==Node.Type.LEAF) {
        			
                	editThisNode.setResponse(newResponseText);
        			
        		} 
        		/**
        		 * Replace Text Only
        		 */
        		else {
        			
        			oldResponseText = editThisNode.response();
        	        oldResponseText = oldResponseText.substring(oldResponseText.lastIndexOf(","));
        			
        	        editThisNode.setResponse(newResponseText + oldResponseText);
        			
        		}
            	
            }
    	}
    	
    }    
    
    /**
     * Simple Replace All Text with the new text
     * @param replaceToMap
     */
    public void replaceAll(Map<String, String> replaceToMap) {
    	
    	String jsonString = this.context.toJSONString();
    	
    	for (Map.Entry<String, String> entry:replaceToMap.entrySet()) {
    		jsonString = jsonString.replace(entry.getKey(), entry.getValue());
    	}
    	
    	this.context.clear();
    	this.context.attr().clear();
    	this.context.prop().clear();
    	this.context.loadJSON(jsonString);
    	
    }
    
    /**
     * Simple Nodes Query
     * @param keywords
     * @param match
     * @return
     */
    public List<Node> query(String keywords, Match match) {
    	
    	/**
    	 * Ignore case
    	 */
    	keywords = keywords.toLowerCase();
    	
    	List<Node> nodeList = new ArrayList<>();
    	
    	String label;
    	
    	for (Node node:this.context.nodeList()) {
    		
			label = this.hooksToKeywords(node.hookList()).toLowerCase();//Ignore Parent Id & Case
			
			if (match==Match.Head && label.startsWith(keywords)) {
				nodeList.add(new Node(node));			
			}
			
			if (match==Match.Tail && label.endsWith(keywords)) {
				nodeList.add(new Node(node));			
			}
			
			if (match==Match.All && label.equals(keywords)) {
				nodeList.add(new Node(node));
			}
			
			if (match==Match.Body && label.contains(keywords)) {
				nodeList.add(new Node(node));
			}
    	}
    	
    	return nodeList;
    }
    
    public int remove(String rootKeywords) {
    	
    	int matchRootKeywordsCount = 0;
    	
    	List<Node> matchedNode = new ArrayList<>();
    	
    	String label;
    	
    	for (Node node:this.context.nodeList()) {
    		
			label = this.hooksToKeywords(node.hookList());
			
			//Whole Matched 
			if (rootKeywords.equals(label)) {
				
				matchedNode.add(node);
			}
    		
    	}
    	
    	matchRootKeywordsCount = matchedNode.size();
    	
    	Map<Node, Set<Node>> parentMap = new HashMap<>();
    	
    	for (Node node:matchedNode) {
    		
    		recursiveCollectChild (this.context.nodeList(), parentMap, node);
    	}
    	
    	matchedNode.addAll(parentMap.keySet());

    	List<Map.Entry<String, String>> removeConnectionList = new ArrayList<>();
    	
    	for (final Map.Entry<Node, Set<Node>> entry:parentMap.entrySet()) {
    		
    		for (final Node node:entry.getValue()) {
    			
    			removeConnectionList.add(new Map.Entry<String, String>() {

					@Override
					public String getKey() {
						// TODO Auto-generated method stub
						return (String) node.attr("id");
					}

					@Override
					public String getValue() {
						// TODO Auto-generated method stub
						return (String) entry.getKey().attr("id");
					}

					@Override
					public String setValue(String value) {
						throw new UnsupportedOperationException();
					}
				});
    			
    		}
    		
    	}
    	
    	/*
    	for (Map.Entry<String, String> removeConnection:removeConnectionList) {
    		
    		System.out.println(removeConnection.getKey() + "->" + removeConnection.getValue());
    	
    	} 
    	*/
    	
    	/**
    	 * Remove from context
    	 */
    	for (Node removeNode:matchedNode) {
    		this.context.nodeList().remove(removeNode);
    	}
    	
    	/**
    	 * Remove connections
    	 */
    	List<Map<String, String>> connectionList = (List<Map<String, String>>) context.attr("connections");
        
    	Map<String, String> connectionMap;

        String sourceId, targetId;
        
        List<Map<String, String>> willRemoveConnectionList = new ArrayList<>();
        for (int i=0; i<connectionList.size(); i++) {
        	connectionMap = connectionList.get(i);
        	
        	sourceId = connectionMap.get("source");
        	targetId = connectionMap.get("target");
        	
        	for (Map.Entry<String, String> removeConnection:removeConnectionList) {
        		
        		if (sourceId.equals(removeConnection.getKey()) &&
        				targetId.equals(removeConnection.getValue())) {
        			willRemoveConnectionList.add(connectionMap);
        			break;
        		}
        	
        	}
        	
        }
        
    	for (Map<String, String> removeConnection:willRemoveConnectionList) {
    		connectionList.remove(removeConnection);
    	}
    	
    	return matchRootKeywordsCount;
    }
    
    public void removeAll() {
    	    	
    	/**
    	 * Remove connections
    	 */
    	List<Map<String, String>> connectionList = (List<Map<String, String>>) context.attr("connections");
    	
    	if (connectionList!=null) {
    		
        	connectionList.clear();
        	
    	}
    	
    	context.nodeList().clear();    	
        
    }    
    
}
