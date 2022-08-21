package com.wayos.connector;

import org.json.JSONArray;
import org.json.JSONObject;

import com.wayos.command.talk.Question;

import java.util.ArrayList;
import java.util.List;

public class ResponseObject {

    public interface JSONAble {
        JSONObject toJSONObject();
    }

    public static class Text implements JSONAble {
        private final String text;
        public Text(String text) {
            this.text = text;
        }
        @Override
        public String toString() {
            return text;
        }
        @Override
        public JSONObject toJSONObject() {
            JSONObject obj = new JSONObject();
            obj.put("text", text);
            return obj;
        }
    }
    
    public static abstract class Media implements JSONAble {
    	
        protected final String url;
        
    	public Media(String url) {
			this.url = url;
    	}
        @Override
        public String toString() {
            return url;
        }    	
        @Override
        public JSONObject toJSONObject() {
            JSONObject obj = new JSONObject();
            obj.put("url", url);
            return obj;
        }
    }

    public static class Image extends Media {
        public Image(String url) {
        	super(url);
        }
    }

    public static class Audio extends Media {
        public Audio(String url) {
        	super(url);
        }
    }
    
    public static class Video extends Media {
        public Video(String url) {
        	super(url);
        }
    }    

    public final List<Object> messageList = new ArrayList<>();
    
    public final String responseText;

    public ResponseObject(String responseText) {
    	
    	/**
    	 * Preprocess special text command
    	 * [br] new line
    	 */
    	
    	responseText = responseText.replace("[br]", System.lineSeparator()); 
    	
    	this.responseText = responseText;
    	
        String [] responses = this.responseText.split("\n\n\n");
        String [] tokens;

        List<Question> questionList = new ArrayList<>();
        for (String response:responses) {
            tokens = response.split(" ", 2);

            String firstToken = tokens[0].toLowerCase();
            
            if (firstToken.startsWith("question:")) {

        		questionList.add(Question.build(response));

            } else if (firstToken.startsWith("https:")) {
            	            	
            	/**
            	 * Trim Query Parameters (for Facebook Storage)
            	 */
            	
            	if (!firstToken.startsWith("https://wayobot.com") && 
            			!firstToken.startsWith("https://eoss-") && 
            			!firstToken.startsWith("https://flex-") && 
            				firstToken.contains("?")) {
            		
                	firstToken = firstToken.substring(0, firstToken.lastIndexOf("?"));                	
            	}
            	
                if (firstToken.endsWith("png") || firstToken.endsWith("jpg") || firstToken.endsWith("jpeg")|| firstToken.endsWith("gif")) {
                	
                    messageList.add(new Image(tokens[0]));
                    
                } else if (firstToken.endsWith("m4a") || firstToken.endsWith("mp3")) {
                	
                    messageList.add(new Audio(tokens[0]));
                    
                } else if (firstToken.endsWith("mp4")) {
                	
                    messageList.add(new Video(tokens[0]));
                    
                } else {
                	
                    messageList.add(new Text(tokens[0]));
                    
                }

                if (tokens.length==2) {
                	
                    messageList.add(new Text(tokens[1]));
                    
                }

            } else if (!response.trim().isEmpty()) {
                messageList.add(new Text(response));
            }
        }

        boolean hasQuestion = questionList!=null && !questionList.isEmpty();
        if (hasQuestion) {
            messageList.add(questionList);
        }

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        List<Question> questionList;
        for (Object message:messageList) {
            if (message instanceof List) {

                questionList = (List<Question>) message;
                for (Question q:questionList) {
                    sb.append(q + "\n\n\n");
                }

            } else {
                sb.append(message + "\n\n\n");
            }
        }

        return sb.toString().trim();
    }
    
    @Deprecated
    /**
     * HOTFIX!!!
     * To JSON Object format for Android
     * {text:"line1\n\line2\n\line3, questions:[{}, {}]"}
     * @return
     */
    public String toJSONObjectString() {
    	
    	JSONObject object = new JSONObject();
    	
        StringBuilder texts = new StringBuilder();
    	JSONArray questionArray = new JSONArray();
    	
    	StringBuilder questionLabels = new StringBuilder();
    	List<Question> questionList;
        for (Object obj:messageList) {
        	if (obj instanceof List) {
        		
            	questionList = (List<Question>) obj;        		
        		for (Question q:questionList) {
        			questionLabels.append(q.label + System.lineSeparator());
            		questionArray.put(q.toJSONObject());
        		}
        	} else {
            	texts.append(obj.toString() + System.lineSeparator());
        	}        	
        }
        texts.append(questionLabels.toString());
        object.put("text", texts.toString().trim());
        object.put("questions", questionArray);        
    	
    	return object.toString();
    }

    public String toJSONString() {

        JSONArray array = new JSONArray();

        JSONArray questionArray;
        for (Object obj:messageList) {
        	
        	if (obj instanceof List) {
            	
            	List<Question> questionList = (List<Question>) obj;
            	questionArray = new JSONArray();
            	for (Question q:questionList) {
            		questionArray.put(q.toJSONObject());
            	}
            	array.put(questionArray);
            	
            } else if (obj instanceof JSONAble) {
            	
                array.put((((JSONAble) obj).toJSONObject()));
                
            } else {
            	throw new IllegalArgumentException("Unknown type for " + obj.getClass());
            }
        	
        }

        return array.toString();
    }
    
    public static void main(String [] args) {
    	
    	ResponseObject responseObject = new ResponseObject("https://wayobot.com/bin/1833768260014999/4.1%20RD%20BRIGHT%2024.png");
    	System.out.println(responseObject.messageList);
    	
    }

}