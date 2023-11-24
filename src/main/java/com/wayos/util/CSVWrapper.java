package com.wayos.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.wayos.Context;
import com.wayos.Hook;
import com.wayos.Node;

public class CSVWrapper {
	
	private Context context;
	
	private String delimeter;
	
	public CSVWrapper(Context context, String delimeter) {
		
		this.context = context;
        this.delimeter = delimeter;
	}
	
	@Override
	public String toString() {
		
        /**
         * No	Keywords	Answer	IsQuestion	Next	Expressions	W	X	Y
         * 1	greeting
         * 2	unknown
         * 3	silent
         * 4
         * 5
         * ..
         */
        
        //Build Tree Map
    	Map<String, List<String>> childrenMap = new HashMap<>();
    	Map<String, Node> nodeMap = new HashMap<>();
    	
    	String id, parentId;
    	List<String> children;
    	
        for (Node node:context.nodeList()) {
        	
        	id = (String) node.attr("id");
        	if (id==null) continue;
        	
        	for (Hook hook: node.hookList()) {
        		
        		if (hook.text.startsWith("@")) {
        			
        			parentId = hook.text.substring(1);
        			
        			children = childrenMap.get(parentId);
        			
        			if (children==null) {
        				children = new ArrayList<>();
        				childrenMap.put(parentId, children);
        			}
    				children.add(id);
        			
        		}
        	}
        	
        	nodeMap.put(id, node);        	
        }
        
        List<Node> nodeList = new ArrayList<>();
        
        //@id to number mapper
        Map<String, Integer> idMap = new HashMap<>();
                                        
        List<Node> configNodeList = configNodeList(nodeMap, childrenMap);
                
        for (Node node:configNodeList) {
        	
        	if (nodeList.contains(node)) continue;
        	
        	nodeList.add(node);
        	
        	String nodeId = (String) node.attr("id");
        	
        	idMap.put(nodeId, nodeList.size());
        	
        	nodeMap.remove(nodeId);
        	
            recursiveBuildNodeList(node, nodeList, nodeMap, childrenMap, idMap);        	
        	        	
        }
        
        //Add floating nodes
        for (Map.Entry<String, Node> entry: nodeMap.entrySet()) {
        	
        	nodeList.add(entry.getValue());
        	idMap.put(entry.getKey(), nodeList.size());
        }
        
        if (context.nodeList().size() != nodeList.size()) throw new RuntimeException("Export to CSV Error From " + context.nodeList().size() + " to " + nodeList.size());
                        
        //Transfrom them to CSV begin with header, greeting, unknown and silent
        
        StringBuilder lines = new StringBuilder("Number" + delimeter + "Keywords" + delimeter + "Answer" + delimeter + "Question" + delimeter + "Next" + delimeter + "Expressions" + delimeter + "W" + delimeter + "X" + delimeter + "Y" + System.lineSeparator());
        
        /**
         * Extract custom properties
         */
        for (Map.Entry<String, String> entry:context.sortedPropertiesMap().entrySet()) {
        	
        	lines.append("" + delimeter + entry.getKey() + delimeter + entry.getValue()+ delimeter + "" + delimeter + "" + delimeter + "" + delimeter + "" + delimeter + "" + delimeter + "" + System.lineSeparator());
        }
                
        lines.append("1" + delimeter + config("greeting", childrenMap, idMap) + System.lineSeparator());
        lines.append("2" + delimeter + config("unknown", childrenMap, idMap) + System.lineSeparator());
        lines.append("3" + delimeter + config("silent", childrenMap, idMap) + System.lineSeparator());
        
        int offset = 3;//Next from greeting, unknown, silent
        String nodeId;
        String line, number, keywords, answer, isQuestion, next, expressions, x, y, w;
        String responseText;
        String [] responseTokens;
        int lastIndexOfComma;
        int indexOfStartExpression;
        
        for (Node node:nodeList) {
        	
        	nodeId = (String) node.attr("id");
        	
        	number = number(nodeId, idMap, offset);
        	keywords = keywords(node);
        	
        	responseText = node.response();
        	
        	responseText = responseText == null ? "" : responseText.trim();
        	
       		answer = responseText;
       		expressions = "";
       		
       	    if (node.type() != Node.Type.LEAF) {
        		
        		lastIndexOfComma = responseText.lastIndexOf(", ");
        		
        		if (lastIndexOfComma!=-1) {
        			
        			answer = responseText.substring(0, lastIndexOfComma);
        			
        			responseText = responseText.substring(lastIndexOfComma + 2, responseText.length()-1);
        			            		            		
            		responseTokens = responseText.split(" ");
            		
            		for (String token:responseTokens) {
            			token = token.trim();
            			if (token.startsWith("@")) continue;
            			expressions += token + " ";
            		}
            		
            		expressions = expressions.trim();
            		
        		}
        		
        	} else {
        		
        		/**
        		 * answer `<expr1>` .. `<exprN>`
        		 */
        		indexOfStartExpression = answer.indexOf("`");
        		
        		if (indexOfStartExpression!=-1 && 
        				indexOfStartExpression != answer.length() - 1 && 
        						answer.endsWith("`")) {
        			
        			expressions = answer.substring(indexOfStartExpression);
        			
        			answer = answer.substring(0, indexOfStartExpression);
        			answer = answer.trim();
        			
        		}
        		
        	}
       	    
       	    answer = answer.replace("\n", "[br]");
       	    expressions = expressions.replace("\n", "[br]");
       	    
       	    Node.Type type = node.type();
       	    
       	    if (type==Node.Type.QUESTIONER) {
       	    	
        		isQuestion = "yes";
        		
       	    } else {
       	    	
        		isQuestion = "";
        		
       	    }

        	next = next(nodeId, childrenMap, idMap, offset);
        	
        	x = node.attr("x").toString();
        	y = node.attr("y").toString();
        	w = toTextWeights(node.hookList());
        	
        	line = number + delimeter + keywords + delimeter + answer + delimeter + isQuestion + delimeter + next + delimeter + expressions + delimeter + w + delimeter + x + delimeter + y;
        	
        	lines.append(line + System.lineSeparator());
        }
                        
        return lines.toString().trim();				
	}
	
	private String toTextWeights(List<Hook> hookList) {
		
		StringBuilder sb = new StringBuilder();
		
		for (Hook h:hookList) {
			
			if (h.text.startsWith("@")) continue;//Skip id
			
			sb.append(h.weight + " ");
		}
		
		return sb.toString().trim();
	}

	private List<Node> configNodeList(Map<String, Node> nodeMap, Map<String, List<String>> childrenMap) {
		
		List<String> configList = Arrays.asList(
				context.prop("greeting"), 
				context.prop("unknown"), 
				context.prop("silent"));
		
		List<Node> configNodeList = new ArrayList<>();
		
		String nodeId;
		Node node;
		List<String> childList;
		String parentNodeId;
		
        for (String config: configList) {
        	
        	parentNodeId = getNextId(config);
        	
            if (parentNodeId!=null) {
            	
            	childList = childrenMap.get(parentNodeId);
            	if (childList!=null && childList.size()>0) {
            		nodeId = childList.get(0);
            		node = nodeMap.get(nodeId);
            		
            		configNodeList.add(node);
            	}
            	
            }         	
        }
        
        return configNodeList;		
	}
	
    private String config(String name, Map<String, List<String>> childrenMap, Map<String, Integer> idMap) {
    	
    	String property = context.prop(name);
    	    	
    	String nextId = getNextId(property);
    	
    	if (property==null) {
    		property = "";
    	}
    	
    	Map attr;
    	
    	if (name.equals("greeting")) {
    		
    		attr = (Map)context.attr("start");
    		
    	} else if (name.equals("unknown")) {
    		
    		attr = (Map)context.attr("end");    		
    		
    	} else {
    		
    		attr = (Map)context.attr("silent");  		    		
    		
    	}
    	
    	String x;
    	try {
    		x = attr.get("x").toString();
    	} catch (Exception e) {
    		x = "";
    	}
    	
    	String y;
    	try {
    		y = attr.get("y").toString();
    	} catch (Exception e) {
    		y = "";
    	}
    	
    	if (nextId==null) {
    		
    		return name + delimeter + property + delimeter + "" + delimeter + "" + delimeter + "" + delimeter + "" + delimeter + x + delimeter + y;
    	} 
    	
    	if (property.contains(", ")) {
    		
    		property = property.substring(0, property.indexOf(", "));
    	}
    	
		return name + delimeter + property + delimeter + "" + delimeter + next(nextId, childrenMap, idMap, 3) + delimeter + "" + delimeter + "" + delimeter + x + delimeter + y;
    }
    
    private String number(String nodeId, Map<String, Integer> idMap, int offset) {
    	
    	return "" + (idMap.get(nodeId) + offset);
    }
    
    private String keywords(Node node) {  
    	
        StringBuilder sb = new StringBuilder();
        
        for (Hook hook:node.hookList()) {
        	
        	if (hook.text.startsWith("@")) continue;
        	
        	if (hook.match == Hook.Match.GreaterEqualThan) {
        		sb.append("gte");
        	}
        	if (hook.match == Hook.Match.GreaterThan) {
        		sb.append("gt");
        	}
        	if (hook.match == Hook.Match.LowerEqualThan) {
        		sb.append("lte");
        	}
        	if (hook.match == Hook.Match.LowerThan) {
        		sb.append("lt");
        	}
        	
            sb.append(hook.text);
            sb.append(" ");
        }
        return sb.toString().trim();
    }
    
    private String next(String nodeId, Map<String, List<String>> childrenMap, Map<String, Integer> idMap, int offset) {
    	
    	String nexts = "";
    	List<String> children = childrenMap.get(nodeId);
    	
    	if (children!=null) {
        	for (String childId: children) {
        		nexts += (idMap.get(childId) + offset) + ",";
        	}    		
    	}
    	
    	if (nexts.endsWith(",")) {
    		nexts = nexts.substring(0, nexts.length()-1);
    	}
    	
    	return nexts;
    }
        
    /**
     * Near Travel First
     * @param node
     * @param nodeList
     * @param nodeMap
     * @param childrenMap
     * @param idMap
     */
    private void recursiveBuildNodeList(Node node, List<Node> nodeList, Map<String, Node> nodeMap, Map<String, List<String>> childrenMap, Map<String, Integer> idMap) {
    
    	if (node==null) return;
    		
    	String nodeId = (String) node.attr("id");
    	    	    	    	
    	List<String> children = childrenMap.get(nodeId);
    	
    	if (children!=null) {
    		
        	Node childNode;
        	
        	List<String> goList = new ArrayList<>();
        	for (String childId: children) {
        		
            	if (idMap.containsKey(childId)) continue; //Protect from cyclic add
            	
        		childNode = nodeMap.get(childId);
        		        		
        		if (childNode==null) continue;
        		
            	nodeList.add(childNode);
            	            	
            	idMap.put(childId, nodeList.size());
            	
            	goList.add(childId);
        		
        	}    		
        	
        	for (String childId: goList) {
        		        		
        		childNode = nodeMap.get(childId);
        		
        		recursiveBuildNodeList(childNode, nodeList, nodeMap, childrenMap, idMap);
        		
            	nodeMap.remove(childId);
        	}
        	
    	}
    	    	    	
    }

	private String getNextId(String responseText) {
		
		if (responseText==null) return null;
		
		responseText = responseText.trim();
		
		if (!responseText.endsWith("!")) {
			
			return null;
		}
		
		responseText = responseText.substring(0, responseText.length()-1);
		
		int lastIndexOfAddr = responseText.lastIndexOf("@");		
		
		responseText = responseText.substring(lastIndexOfAddr + 1);
		
		return responseText.trim();
	}	

}
