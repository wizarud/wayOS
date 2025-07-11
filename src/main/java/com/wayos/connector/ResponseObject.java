package com.wayos.connector;

import com.wayos.command.talk.Question;
import com.wayos.util.URLInspector;

import x.org.json.JSONArray;
import x.org.json.JSONObject;

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
            
            obj.put("type", "text");
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

    public static class Link extends Media {
    	
        public Link(String url) {
        	super(url);
        }
        
        @Override
        public JSONObject toJSONObject() {
        	
    		JSONObject imageObject = new JSONObject();
    		
    		imageObject.put("type", "link");
    		imageObject.put("src", url);
    		
    		return imageObject;
        }
    }
    
    public static class Widget extends Media {
    	
        public Widget(String url) {
        	super(url);
        }
        
        @Override
        public JSONObject toJSONObject() {
        	
    		JSONObject imageObject = new JSONObject();
    		
    		imageObject.put("type", "widget");
    		imageObject.put("src", url);
    		
    		return imageObject;
        }
    }
    
    public static class Image extends Media {
    	
        public Image(String url) {
        	super(url);
        }
        
        @Override
        public JSONObject toJSONObject() {
        	
    		JSONObject imageObject = new JSONObject();
    		
    		imageObject.put("type", "image");
    		imageObject.put("src", url);
    		
    		return imageObject;
        }
    }

    public static class Audio extends Media {
    	
        public Audio(String url) {
        	super(url);
        }
        
        @Override
        public JSONObject toJSONObject() {
    		
    		JSONObject audioObject = new JSONObject();
    		
    		audioObject.put("type", "audio");
    		audioObject.put("src", url);
    		
    		return audioObject;
    	}
        
    }
    
    public static class Video extends Media {
    	
        public Video(String url) {
        	super(url);
        }
        
        @Override
        public JSONObject toJSONObject() {
    		
    		JSONObject videoObject = new JSONObject();
    		
    		videoObject.put("type", "video");
    		videoObject.put("src", url);
    		
    		return videoObject;
    	}
    }    

    public final List<Object> messageList = new ArrayList<>();
    
    public final String responseText;

    public ResponseObject(String responseText) {
    	
    	/**
    	 * Preprocess special text command
    	 * [br] new line
    	 */
    	
    	//responseText = responseText.replace("[br]", System.lineSeparator()); 
    	
    	//TODO: For call another bot and place question result on question
    	/*
    	if (responseText.startsWith("Question:") && responseText.contains("Id:")) {
    	}
    	*/
     	
    	this.responseText = responseText;
    	
        String [] responses = this.responseText.split("\n\n\n");
        String [] tokens;

        List<Question> questionList = new ArrayList<>();
        
        String tokens0LowerCase;
        
        for (String response:responses) {
        	
            tokens = response.split(" ", 2);
            
            tokens0LowerCase = tokens[0].toLowerCase();//For Media check

            if (tokens[0].startsWith("Question:") && response.contains("Id:")) {

        		questionList.add(Question.build(response));

            } else if (URLInspector.isValid(tokens[0]) || tokens[0].startsWith("/public/")) {
            	            	
                if (URLInspector.isImage(tokens[0])) {
                	
                    messageList.add(new Image(tokens[0]));
                    
                } else if (tokens0LowerCase.endsWith("ogg") || tokens0LowerCase.endsWith("m4a") || tokens0LowerCase.endsWith("mp3") || tokens0LowerCase.endsWith("wav")) {
                	
                    messageList.add(new Audio(tokens[0]));
                    
                } else if (tokens0LowerCase.endsWith("mp4")) {
                	
                    messageList.add(new Video(tokens[0]));
                    
                } else if (URLInspector.isWidget(tokens[0])) { 
                    
                    messageList.add(new Widget(tokens[0]));
                    
                } else {
                	
                    messageList.add(new Link(tokens[0])); 
                    
                }

                //Append tail as a message
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

        //System.out.println(this.toJSONString());
        
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
            	
            	//Widget Test
            	if (questionArray.length()==1) {
            		String linkURL = questionArray.getJSONObject(0).optString("linkURL");
            		if (URLInspector.isWidget(linkURL)) {
            			questionArray.getJSONObject(0).put("type", "widget");
            		}
            	}
            	
            	JSONObject questionObject = new JSONObject();
            	
            	questionObject.put("type", "menus");            	            	
            	questionObject.put("menus", questionArray);
            	
            	array.put(questionObject);
            	
            } else if (obj instanceof JSONAble) {
            	
                array.put((((JSONAble) obj).toJSONObject()));
                
            } else {
            	
            	throw new IllegalArgumentException("Unknown type for " + obj.getClass());
            	
            }
        	
        }

        return array.toString();
    }
    
    public static void main(String [] args) {
    	
    	//ResponseObject responseObject = new ResponseObject("https://wayos.yiem.cc/flatten?layers=https://wayos.yiem.cc/public/eoss-th/10.3%20PK%20Lotus%2038.png&layers=https%3A%2F%2Fwayos.yiem.cc%2Fpublic%2Feoss-th%2Fdirector.PNG&type=png");
    	//System.out.println(responseObject.messageList);
    	
    	ResponseObject responseObject = new ResponseObject("สำหรับ test ขออนุญาติตอบลงใน inbox นะครับ\n"
    			+ "\n"
    			+ "\n"
    			+ "นี่คือข้อความอัตโนมัติ รบกวนฝากข้อความ ทางเราจะรีบติดต่อกลับไปครับ\n"
    			+ "\n"
    			+ "\n"
    			+ "https://wayos.yiem.cc/public/1833768260014999/F8646F3F-AD60-4627-9D7F-A6773E15C762.jpeg\n"
    			+ "\n"
    			+ "\n"
    			+ "\n"
    			+ "https://wayos.yiem.cc/public/1833768260014999/EE2324D2-9521-453C-A081-C914DF2D3988.jpeg");
    	System.out.println(responseObject.messageList.get(3).getClass().getSimpleName());
    }

}