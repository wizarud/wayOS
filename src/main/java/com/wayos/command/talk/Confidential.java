package com.wayos.command.talk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import com.wayos.Node;

public class Confidential {
	
    public static Node maxActiveNodeFrom(Set<Node> activeNodeSet) {
    	
        if (activeNodeSet==null || activeNodeSet.isEmpty()) return null;

        float maxActive = Float.MIN_VALUE;
        Node maxActiveNode = null;

        for (Node node:activeNodeSet) {
        	
            if (node.active()>maxActive) {
            	
                maxActive = node.active();
                maxActiveNode = node;
                
            }
            
        }

        return maxActiveNode;
    }

    public static Node maxActiveNodeFrom(Set<Node> activeNodeSet, Random random) {

        if (activeNodeSet==null || activeNodeSet.isEmpty()) return null;

        TreeMap<Float, List<Node>> nodeMap = new TreeMap<>();

        Float confidence;
        List<Node> nodeList;
        
        for (Node node:activeNodeSet) {
        	
            confidence = node.active();
            nodeList = nodeMap.get(confidence);
            
            if (nodeList==null) nodeList = new ArrayList<>();
            
            nodeList.add(node);
            nodeMap.put(confidence, nodeList);
            
        }

        Map.Entry<Float, List<Node>> maxActiveEntry = nodeMap.lastEntry();
        List<Node> maxActiveNodeList = maxActiveEntry.getValue();

        return maxActiveNodeList.get(random.nextInt(maxActiveNodeList.size()));
    }
    
    public static List<Node> maxActiveNodeListFrom(Set<Node> activeNodeSet) {
    	
    	return maxActiveNodeListFrom(new ArrayList<>(activeNodeSet));
    }
    
    public static List<Node> maxActiveNodeListFrom(List<Node> activeNodeList) {

        if (activeNodeList==null || activeNodeList.isEmpty()) return null;
        
        TreeMap<Float, List<Node>> nodeMap = new TreeMap<>();

        Float confidence;
        List<Node> nodeList;
        
        for (Node node:activeNodeList) {
        	
            confidence = node.active();
            nodeList = nodeMap.get(confidence);
            
            if (nodeList==null) {
            	
            	nodeList = new ArrayList<>();
            }
            
            nodeList.add(node);
            nodeMap.put(confidence, nodeList);
        }

        Map.Entry<Float, List<Node>> maxActiveEntry = nodeMap.lastEntry();
        
        return maxActiveEntry.getValue();
    }
    
    public static TreeMap<Float, List<Node>> confidenceActiveNodeListMap(List<Node> activeNodeList) {

        if (activeNodeList==null || activeNodeList.isEmpty()) return null;
        
        TreeMap<Float, List<Node>> nodeMap = new TreeMap<>();

        Float confidence;
        List<Node> nodeList;
        
        for (Node node:activeNodeList) {
        	
            confidence = node.active();
            nodeList = nodeMap.get(confidence);
            
            if (nodeList==null) {
            	
            	nodeList = new ArrayList<>();
            }
            
            nodeList.add(node);
            nodeMap.put(confidence, nodeList);
        }

        return nodeMap;
    }    
    
}
