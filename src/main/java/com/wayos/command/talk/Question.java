package com.wayos.command.talk;

import org.json.JSONArray;
import org.json.JSONObject;

import com.wayos.Hook;
import com.wayos.Node;
import com.wayos.Session;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static java.util.Optional.ofNullable;

public class Question {

    protected ReadWriteLock lock = new ReentrantReadWriteLock();
    
    public final String parent;
    public final String label;
    public final String imageURL;
    public final List<Choice> choices;

    public List<Node> nodeList;
    
    /**
     * Empty Keywords
     */
    public List<Node> defaultChoices;

    public Question(String parent, String label, String imageURL, List<Choice> choices) {
        this.parent = parent;
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
        String firstTitles = titles[0].toLowerCase();
        if (firstTitles.startsWith("https") && (firstTitles.endsWith("png") || firstTitles.endsWith("jpg") || firstTitles.endsWith("jpeg"))) {
            imageURL = titles[0];
            label = title.replace(imageURL, "").trim();
        } else {
            imageURL = null;
            label = title;
        }

        List<String> paramList = Arrays.asList(params.split(" "));
        String foundParent = null;
        for(String param:paramList) {
            if (param.startsWith("@")) {
                foundParent = param;
            }
        }
        parent = foundParent;

        choices = new ArrayList<>();
        nodeList = new ArrayList<>();
        defaultChoices = new ArrayList<>();

        //TODO: should perform load test again!
        lock.readLock().lock();
        try {
        	
            session.context().nodeList().forEach(new Consumer<Node>() {
            	
                @Override
                public void accept(Node node) {

                    List<Hook> hookList = node.hookList();

                    /**
                     * Is Child
                     */
                    boolean matched = false;
                    for (Hook hook:hookList) {
                        if (paramList.contains(hook.text)) {
                            matched = true;
                            break;
                        }
                    }

                    if (!matched) return;

                    boolean isDefaultChoice = true;
                    String label = "";
                    for (Hook hook:hookList) {
                        if (hook.text.startsWith("@")) {
                            continue;
                        }

                        isDefaultChoice = false;
                        if (hook.text.contains(",") || hook.text.contains("*")) {
                            continue;
                        }
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

                    if (imageURL.startsWith("https://") &&
                            (imageURL.endsWith("png") ||
                                    imageURL.endsWith("jpg") ||
                                    imageURL.endsWith("jpeg"))) {
                        imageURL = responses[0].trim();
                    } else {
                        imageURL = null;
                    }

                    String linkURL = responses[responses.length-1].trim();

                    if ( (!linkURL.startsWith("https://") || 
                    		!linkURL.contains("://") /*For App*/) && 
                    		!linkURL.startsWith("tel:") && 
                    		!linkURL.startsWith("mailto:") ) {
                        linkURL = null;
                    } else {
                        /**
                         * Variable Supported for Link
                         */
                        linkURL = session.parameterized(null, linkURL);
                    }

                    choices.add(new Choice(parent, label, imageURL, linkURL));

                    //Resplit Hook by Locale
                    Node tempNode = Node.build(session.context().split(Hook.toString(hookList)));
                    tempNode.setResponse(node.response());
                    tempNode.attr("isQuestion", node.attr("isQuestion"));
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
                        ofNullable(parent).orElse(""),
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
        questionObj.put("parent", parent);
        questionObj.put("label", label);
        questionObj.put("imageURL", imageURL);

        JSONArray choiceArray = new JSONArray();
        JSONObject choiceObj;
        for (Choice choice:choices) {
            choiceObj = new JSONObject();
            choiceObj.put("parent", choice.parent);
            choiceObj.put("label", choice.label);
            choiceObj.put("imageURL", choice.imageURL);
            choiceObj.put("linkURL", choice.linkURL);
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
    
}