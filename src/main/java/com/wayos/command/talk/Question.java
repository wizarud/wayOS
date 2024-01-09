package com.wayos.command.talk;

import com.wayos.ContextListener;
import com.wayos.Hook;
import com.wayos.MessageObject;
import com.wayos.Node;
import com.wayos.NodeEvent;
import com.wayos.Session;
import com.wayos.util.URLInspector;

import x.org.json.JSONArray;
import x.org.json.JSONObject;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;

import static java.util.Optional.ofNullable;

public class Question {

    protected ReadWriteLock lock = new ReentrantReadWriteLock();
    
    public final String id;
    public final String label;
    public final String imageURL;
    public final List<Choice> choices;

    public List<Node> nodeList;
    
    /**
     * Empty Keywords
     */
    public List<Node> defaultChoices;

    public Question(String id, String label, String imageURL, List<Choice> choices) {
        this.id = id;
        this.label = label;
        this.imageURL = imageURL;
        this.choices = choices;
    }

    public Question(Session session, String title, String params) {

        /**
         * Variable Supported for imageURL & label
         */
    	title = session.parameterized(null, title);
        
        String [] titles = title.split(" ");
        
        if (URLInspector.isValid(titles[0]) && URLInspector.isImage(titles[0])) {
        	
            imageURL = titles[0];
            label = title.replace(imageURL, "").trim();
            
        } else {
        	
            imageURL = null;
            label = title;
            
        }
        
    	MessageObject messageObject = MessageObject.build(params);
    	messageObject.split();

        List<String> paramList = messageObject.wordList();
        String parentId = null;
        for(String param:paramList) {
            if (param.startsWith("@")) {
            	parentId = param;
                break;
            }
        }
        id = parentId;

        choices = new ArrayList<>();
        nodeList = new ArrayList<>();
        defaultChoices = new ArrayList<>();

        //TODO: should perform load test again!
        lock.readLock().lock();
        try {
        	        	
            session.context().matched(messageObject, new ContextListener() {
            	
                @Override
                public void callback(NodeEvent nodeEvent) {
                	
                	Node node = nodeEvent.node;
                	
                    List<Hook> hookList = node.hookList();
                	
                    boolean isDefaultChoice = true;
                    String label = "";
                    for (Hook hook:hookList) {
                    	
                    	//Skip id
                        if (hook.text.startsWith("@")) {
                            continue;
                        }

                        isDefaultChoice = false;
                        
                        //TODO: No need to skip soft key for the choices of question?
                        /*
                        if (hook.text.contains("|") || hook.text.contains("*")) {
                            continue;
                        }
                        */
                        
                        label += hook.text + " ";
                    }

                    if (isDefaultChoice) {
                        defaultChoices.add(node);
                        return;
                    }

                    label = label.trim();

                    if (label.isEmpty()) return;
                    
                    /**
                     * Variable Supported for label
                     */
                    label = session.parameterized(null, label);
                    
                    String [] responses = node.response().split(" ");

                    String imageURL = responses[0].trim().toLowerCase();

                    if ((imageURL.startsWith("https://") || imageURL.startsWith("http://")) &&
                            (imageURL.endsWith("png") ||
                                    imageURL.endsWith("jpg") ||
                                    imageURL.endsWith("jpeg"))) {
                        imageURL = responses[0].trim();
                    } else {
                        imageURL = null;
                    }

                    String linkURL = responses[responses.length-1].trim();

                    if (linkURL.contains("://") /*For Web or App*/ ||
                    		linkURL.startsWith("tel:") || 
                    		linkURL.startsWith("mailto:") ) {
                        /**
                         * Variable Supported for Link
                         */
                        linkURL = session.parameterized(null, linkURL);
                    } else {
                        linkURL = null;
                    }
                    
                    choices.add(new Choice(id, label, imageURL, linkURL));

                    //Resplit Hook by Locale
                    Node tempNode = Node.build(session.context().split(Hook.toString(hookList)));
                    tempNode.setResponse(node.response());
                    tempNode.attr("isQuestion", node.attr("isQuestion"));
                    tempNode.attr("hasParam", node.attr("hasParam"));
                    
                    nodeList.add(tempNode);
                    
                }
            });
            
            
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean hasImage() {
        return imageURL != null;
    }
    
    public boolean isLink() {
    	return URLInspector.isValid(label);
    }

    /**
     * @return
     * Question: <Label>
     * Id: <parent>
     * Image: <imageURL>
     *
     * Choice 1 label   imageURL    link
     * Choice 2
     * ...
     */
    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder(
                String.format("Question:%s\nId:%s\nImage:%s",
                        ofNullable(label).orElse(""),
                        ofNullable(id).orElse(""),
                        ofNullable(imageURL).orElse("")
                )
        );
        
        sb.append("\n");
        for (Choice choice: choices) {
            sb.append("\n\t" + choice);
        }

        return sb.toString().trim();
    }

    public JSONObject toJSONObject() {

        JSONObject questionObj = new JSONObject();
        questionObj.put("parent", id);
        questionObj.put("label", label);
        if (isLink()) {
        	questionObj.put("linkURL", label);        	
        }
        if (hasImage()) {
            questionObj.put("imageURL", imageURL);        	
        }
        
        JSONArray choiceArray = new JSONArray();
        JSONObject choiceObj;
        for (Choice choice:choices) {
            choiceObj = new JSONObject();
            choiceObj.put("parent", choice.parent);
            choiceObj.put("label", choice.label);
            
            if (choice.isLinkLabel())
                choiceObj.put("linkURL", choice.linkURL);
            if (choice.isImageLinkLabel())
            	choiceObj.put("imageURL", choice.imageURL);
            choiceArray.put(choiceObj);
        }
        
        questionObj.put("choices", choiceArray);

        return questionObj;
    }

    public static String toString(List<Question> questionList) {
        StringBuilder sb = new StringBuilder();
        for (Question question:questionList) {
            sb.append(question + "\n\n\n");
        }
        return sb.toString().trim();
    }

    public static Question build(String text) {
    	    	
    	text = text.replaceFirst("Question:", "");
    	
    	String label = text.substring(0, text.indexOf("Id:"));
    	
    	text = text.replaceFirst(Pattern.quote(label), "");
    	
        String [] lines = text.split("\n");
        
        if (lines.length < 2) {
        	System.err.println(label);
        	throw new IllegalArgumentException(text);
        }
        
        label = label.trim();
        String parentId = lines[0].replaceFirst("Id:", "").trim();
        String imageURL = lines[1].replaceFirst("Image:", "").trim();
        
        List<Choice> choiceList = new ArrayList<>();

        String line;
        for (int i=3;i<lines.length;i++) {
        	
        	line = lines[i].trim();        	
            choiceList.add(Choice.build(parentId, line));
        }

        parentId = parentId.isEmpty() ? null : parentId;
        imageURL = imageURL.isEmpty() ? null : imageURL;

        return new Question(parentId, label, imageURL, choiceList);
    }
    
    public static void main(String[]args) {
    	String text = "Question:ส่ง Art Work รูปที่จะ Screen ได้เลย (หลังจากส่งรูปแล้วกรุณารอสักครู่นะครับ..)\n"
    			+ "Id:@741c7c40-4798-4a1d-863b-f1c12c5225d2\n"
    			+ "Image:";
    	
    	text = text.replaceFirst("Question:", "");
    	
    	String label = text.substring(0, text.indexOf("Id:"));
    	
    	System.out.println(label);
    }
    
}